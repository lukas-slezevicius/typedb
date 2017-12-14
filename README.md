![GRAKN.AI](https://grakn.ai/img/Grakn%20logo%20-%20transparent.png)

---
[![GitHub release](https://img.shields.io/github/release/graknlabs/grakn.svg)](https://github.com/graknlabs/grakn/releases/latest)
[![Build Status](https://travis-ci.org/graknlabs/grakn.svg?branch=internal)](https://travis-ci.org/graknlabs/grakn)
[![Coverage Status](https://codecov.io/gh/graknlabs/grakn/branch/master/graph/badge.svg)](https://codecov.io/gh/graknlabs/grakn)
[![Javadocs](https://javadoc.io/badge/ai.grakn/grakn.svg)](https://javadoc.io/doc/ai.grakn/grakn)
[![Slack Status](http://grakn-slackin.herokuapp.com/badge.svg)](https://grakn.ai/slack)
[![Stack Overflow][stackoverflow-shield]][stackoverflow-link]
[![Download count](http://shields.grakn.ai/github/downloads/graknlabs/grakn/total.svg)](https://grakn.ai/download/latest)


[stackoverflow-shield]: https://img.shields.io/badge/stackoverflow-grakn-blue.svg
[stackoverflow-link]: https://stackoverflow.com/questions/tagged/grakn

Grakn is a hyper-relational database for knowledge engineering. Rooted in Knowledge Representation and  Automated Reasoning, Grakn provides the knowledge base foundation for intelligent/cognitive systems.

| Get Started | Documentation | Discussion | _Join the Academey!_ |
|:------------|:--------------|:-----------|:---------------------|
| Whether you are new to programming or an experienced developer, it’s easy to learn and use Grakn. Get set up quickly with [quickstart tutorial](https://dev.grakn.ai/docs/get-started/quickstart-tutorial). | Documentation for Grakn’s development library and Graql language API, along with tutorials and guides, are available online. Visit our [documentation portal](https://dev.grakn.ai/). | When you’re stuck on a problem, collaborating helps. Ask your question on [StackOverflow](https://stackoverflow.com/questions/tagged/graql+or+grakn) or discuss it in our [Discussion Forum](https://discuss.grakn.ai/). | _Learn everything from the basic foundations to advanced topics of knowledge engineering and be an expert. Join [Grakn Academy](https://dev.grakn.ai/academy)._|

# Meet Grakn and Graql

Grakn is a hyper-relational database for [knowledge engineering](https://en.wikipedia.org/wiki/Knowledge_engineering). Rooted in [Knowledge Representation and Automated Reasoning](https://en.wikipedia.org/wiki/Knowledge_representation_and_reasoning), Grakn provides the [knowledge base](https://en.wikipedia.org/wiki/Knowledge_base) foundation for [intelligent/cognitive systems](https://en.wikipedia.org/wiki/Knowledge-based_systems). Being a distributed system, Grakn is design to be sharded and replicated over a network of computers. Under the hood, Grakn has built an expressive knowledge representation system based on [hypergraph theory](https://en.wikipedia.org/wiki/Hypergraph) (a subfield in mathematics that generalises an edge to be a set of vertices) with a transactional query interface, Graql. Graql is Grakn’s reasoning (through OLTP) and analytics (through OLAP) declarative query language. 

## Knowledge Schema

Grakn provides an enhanced [entity-relationship](https://en.wikipedia.org/wiki/Entity–relationship_model) schema to model complex datasets. The schema allows users to model type hierarchies, hyper-entities, hyper-relationships and rules. The schema can be updated and extended at any time in the database lifecycle. Hyper-entities are entities with multiple instances of a given attribute, and hyper-relationships are nested relationships, cardinality-restricted relationships, or relationships between any number of entities. This enables the creation of complex knowledge models that can evolve flexibly.

## Logical Inference

Grakn’s query language performs logical inference through [deductive reasoning](https://en.wikipedia.org/wiki/Deductive_reasoning) of entity types and relationships, in order to infer implicit facts, associations and conclusions in real-time, during runtime of OLTP queries. The inference is performed through entity and relationship type reasoning, as well as rule-based reasoning. This allows the discovery of facts that would otherwise be too hard to find, the abstraction of complex relationships into its simper conclusion, as well as translation of higher level queries into lower level and more complex data representation.

## Distributed Analytics

Grakn’s query language performs distributed Pregel and [MapReduce](https://en.wikipedia.org/wiki/MapReduce) ([BSP](https://en.wikipedia.org/wiki/Bulk_synchronous_parallel)) algorithms abstracted as OLAP queries. These types of queries usually require custom development of distributed algorithms for every use case. However, Grakn creates an abstraction of these distributed algorithms and incorporates them as part of the language API. This enables large scale computation of BSP algorithms through a declarative language without the need of implementing the algorithms.

## Higher-Level Language

With the expressivity of the schema, inference through OLTP and distributed algorithms through OLAP, Grakn provides strong abstraction over low-level data constructs and complicated relationships through its query language. The language provides a higher-level schema, OLTP, and OLAP query language, that makes working with complex data a lot easier. When developers can achieve more by writing less code, productivity rate increases by orders of magnitude.

## System Requirements

Operating System: Unix based systems (Linux and Mac OS X)

Running Grakn requires:
- Java 8 (OpenJDK or Oracle Java) with the $JAVA_HOME set accordingly

Compiling Grakn from source requires:
- Maven 3
- nodejs
- yarn installed and configured correctly
- ability to install packages globally for your user via npm without needing sudo

## Licensing

This product includes software developed by [Grakn Labs Ltd](http://grakn.ai/).  It's released under the GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007. For licence information, please see [LICENCE.txt](https://github.com/graknlabs/grakn/blob/master/LICENSE.txt). Grakn Labs Ltd also provides a commercial license for Grakn Enterprise KBMS - get in touch with our team at enterprise@grakn.ai.

Copyright (C) 2016-2017  Grakn Labs Limited.
