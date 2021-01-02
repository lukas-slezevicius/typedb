/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.core.reasoner.resolution.resolver;

import grakn.common.collection.Pair;
import grakn.common.concurrent.actor.Actor;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.logic.Rule;
import grakn.core.logic.resolvable.Concludable;
import grakn.core.logic.transformer.Mapping;
import grakn.core.logic.resolvable.Resolvable;
import grakn.core.logic.resolvable.Retrievable;
import grakn.core.pattern.Conjunction;
import grakn.core.reasoner.resolution.MockTransaction;
import grakn.core.reasoner.resolution.ResolverRegistry;
import grakn.core.reasoner.resolution.answer.AnswerState;
import grakn.core.reasoner.resolution.framework.Request;
import grakn.core.reasoner.resolution.framework.ResolutionAnswer;
import grakn.core.reasoner.resolution.framework.Resolver;
import grakn.core.reasoner.resolution.framework.Response;
import grakn.core.reasoner.resolution.framework.ResponseProducer;
import grakn.core.traversal.TraversalEngine;
import graql.lang.pattern.variable.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static grakn.common.collection.Collections.list;
import static grakn.common.collection.Collections.map;

// TODO unify and materialise in receiveAnswer
public class RuleResolver extends Resolver<RuleResolver> {
    private static final Logger LOG = LoggerFactory.getLogger(RuleResolver.class);

    private final Map<Request, ResponseProducer> responseProducers;
    private final Conjunction conjunction;
    private final Set<Concludable<?>> concludables;
    private final List<Pair<Actor<? extends ResolvableResolver<?>>, Map<Reference.Name, Reference.Name>>> plan;
    private boolean isInitialised;

    public RuleResolver(Actor<RuleResolver> self, Rule rule, ResolverRegistry registry, TraversalEngine traversalEngine) {
        super(self, RuleResolver.class.getSimpleName() + "(rule:" + rule + ")", registry, traversalEngine);
        this.responseProducers = new HashMap<>();
        this.conjunction = rule.when();
        this.concludables = rule.whenConcludables();
        this.plan = new ArrayList<>();
        this.isInitialised = false;
    }

    @Override
    public void receiveRequest(Request fromUpstream, int iteration) {
        LOG.trace("{}: received Request: {}", name(), fromUpstream);

        if (!isInitialised) {
            initialiseDownstreamActors();
            isInitialised = true;
        }

        ResponseProducer responseProducer = mayUpdateAndGetResponseProducer(fromUpstream, iteration);
        if (iteration < responseProducer.iteration()) {
            // short circuit if the request came from a prior iteration
            respondToUpstream(new Response.Exhausted(fromUpstream), iteration);
        } else {
            tryAnswer(fromUpstream, responseProducer, iteration);
        }
    }

    @Override
    protected void receiveAnswer(Response.Answer fromDownstream, int iteration) {
        LOG.trace("{}: received Answer: {}", name(), fromDownstream);

        Request toDownstream = fromDownstream.sourceRequest();
        Request fromUpstream = fromUpstream(toDownstream);
        ResponseProducer responseProducer = responseProducers.get(fromUpstream);

        ResolutionAnswer.Derivation derivation = fromDownstream.sourceRequest().partialResolutions();
        if (fromDownstream.answer().isInferred()) {
            derivation = derivation.withAnswer(fromDownstream.sourceRequest().receiver(), fromDownstream.answer());
        }

        ConceptMap conceptMap = fromDownstream.answer().derived().map();
        Actor<? extends Resolver<?>> sender = fromDownstream.sourceRequest().receiver();
        if (isLast(sender)) {
            if (!responseProducer.hasProduced(conceptMap)) {
                responseProducer.recordProduced(conceptMap);

                ResolutionAnswer answer = new ResolutionAnswer(fromUpstream.partial().aggregateWith(conceptMap).asMapped().toUpstreamVars(),
                                                               conjunction.toString(), derivation, self(), true);
                respondToUpstream(new Response.Answer(fromUpstream, answer), iteration);
            } else {
                tryAnswer(fromUpstream, responseProducer, iteration);
            }
        } else {
            Pair<Actor<? extends ResolvableResolver<?>>, Map<Reference.Name, Reference.Name>> nextPlannedDownstream = nextPlannedDownstream(sender);
            Request downstreamRequest = new Request(fromUpstream.path().append(nextPlannedDownstream.first()),
                                                    AnswerState.UpstreamVars.Initial.of(conceptMap).toDownstreamVars(
                                                            Mapping.of(nextPlannedDownstream.second())),
                                                    derivation);
            responseProducer.addDownstreamProducer(downstreamRequest);
            requestFromDownstream(downstreamRequest, fromUpstream, iteration);
        }
    }

    @Override
    protected void receiveExhausted(Response.Exhausted fromDownstream, int iteration) {
        LOG.trace("{}: Receiving Exhausted: {}", name(), fromDownstream);
        Request toDownstream = fromDownstream.sourceRequest();
        Request fromUpstream = fromUpstream(toDownstream);
        ResponseProducer responseProducer = responseProducers.get(fromUpstream);

        responseProducer.removeDownstreamProducer(fromDownstream.sourceRequest());
        tryAnswer(fromUpstream, responseProducer, iteration);
    }

    @Override
    protected void initialiseDownstreamActors() {
        Set<Concludable<?>> concludablesWithApplicableRules = concludables.stream().filter(c -> c.getApplicableRules().findAny().isPresent()).collect(Collectors.toSet());
        Set<Retrievable> retrievables = Retrievable.extractFrom(conjunction, concludablesWithApplicableRules);
        Set<Resolvable> resolvables = new HashSet<>();
        resolvables.addAll(concludablesWithApplicableRules);
        resolvables.addAll(retrievables);
        // TODO Plan the order in which to execute the concludables
        List<Resolvable> plan = list(resolvables);
        for (Resolvable planned : plan) {
            Pair<Actor<? extends ResolvableResolver<?>>, Map<Reference.Name, Reference.Name>> concludableUnifierPair = registry.registerResolvable(planned);
            this.plan.add(concludableUnifierPair);
        }
    }

    @Override
    protected ResponseProducer responseProducerCreate(Request request, int iteration) {
        Iterator<ConceptMap> traversal = (new MockTransaction(3L)).query(conjunction, new ConceptMap());
        ResponseProducer responseProducer = new ResponseProducer(traversal, iteration);
        Request toDownstream = new Request(request.path().append(plan.get(0).first()),
                                           AnswerState.UpstreamVars.Initial.of(request.partial().map()).toDownstreamVars(Mapping.of(plan.get(0).second())),
                                           new ResolutionAnswer.Derivation(map()));
        responseProducer.addDownstreamProducer(toDownstream);

        return responseProducer;
    }

    @Override
    protected ResponseProducer responseProducerReiterate(Request request, ResponseProducer responseProducerPrevious, int newIteration) {
        assert newIteration > responseProducerPrevious.iteration();
        LOG.debug("{}: Updating ResponseProducer for iteration '{}'", name(), newIteration);

        Iterator<ConceptMap> traversal = (new MockTransaction(3L)).query(conjunction, new ConceptMap());
        ResponseProducer responseProducerNewIter = responseProducerPrevious.newIteration(traversal, newIteration);
        Request toDownstream = new Request(request.path().append(plan.get(0).first()),
                                           AnswerState.UpstreamVars.Initial.of(request.partial().map()).toDownstreamVars(Mapping.of(plan.get(0).second())),
                                           new ResolutionAnswer.Derivation(map()));
        responseProducerNewIter.addDownstreamProducer(toDownstream);
        return responseProducerNewIter;
    }

    @Override
    protected void exception(Exception e) {
        LOG.error("Actor exception", e);
        // TODO, once integrated into the larger flow of executing queries, kill the actors and report and exception to root
    }

    private void tryAnswer(Request fromUpstream, ResponseProducer responseProducer, int iteration) {
        while (responseProducer.hasTraversalProducer()) {
            ConceptMap conceptMap = responseProducer.traversalProducer().next();
            LOG.trace("{}: has found via traversal: {}", name(), conceptMap);
            if (!responseProducer.hasProduced(conceptMap)) {
                responseProducer.recordProduced(conceptMap);
                Optional<AnswerState.UpstreamVars.Derived> derivedAnswer = fromUpstream.partial()
                        .aggregateWith(conceptMap).asUnified().toUpstreamVars();
                if (derivedAnswer.isPresent()) {
                    ResolutionAnswer answer = new ResolutionAnswer(derivedAnswer.get(), conjunction.toString(),
                                                                   ResolutionAnswer.Derivation.EMPTY, self(), true);
                    respondToUpstream(new Response.Answer(fromUpstream, answer), iteration);
                }
            }
        }

        if (responseProducer.hasDownstreamProducer()) {
            requestFromDownstream(responseProducer.nextDownstreamProducer(), fromUpstream, iteration);
        } else {
            respondToUpstream(new Response.Exhausted(fromUpstream), iteration);
        }
    }


    private ResponseProducer mayUpdateAndGetResponseProducer(Request fromUpstream, int iteration) {
        if (!responseProducers.containsKey(fromUpstream)) {
            responseProducers.put(fromUpstream, responseProducerCreate(fromUpstream, iteration));
        } else {
            ResponseProducer responseProducer = responseProducers.get(fromUpstream);
            assert responseProducer.iteration() == iteration ||
                    responseProducer.iteration() + 1 == iteration;

            if (responseProducer.iteration() + 1 == iteration) {
                // when the same request for the next iteration the first time, re-initialise required state
                ResponseProducer responseProducerNextIter = responseProducerReiterate(fromUpstream, responseProducer, iteration);
                responseProducers.put(fromUpstream, responseProducerNextIter);
            }
        }
        return responseProducers.get(fromUpstream);
    }

    private boolean isLast(Actor<? extends Resolver<?>> actor) {
        return plan.get(plan.size() - 1).first().equals(actor);
    }

    Pair<Actor<? extends ResolvableResolver<?>>, Map<Reference.Name, Reference.Name>> nextPlannedDownstream(Actor<? extends Resolver<?>> actor) {
        int index = -1;
        for (int i = 0; i < plan.size(); i++) {
            if (actor.equals(plan.get(i).first())) {
                index = i;
                break;
            }
        }
        assert index != -1 && index < plan.size() - 1 ;
        return plan.get(index + 1);
    }
}
