[<img src=https://user-images.githubusercontent.com/6883670/31999264-976dfb86-b98a-11e7-9432-0316345a72ea.png height=75 />](https://reactome.org)

Interaction Exporter
=====================

This project infers interactions between molecules from reactome.org database.

## Introduction

The Reactome database does not primarily focus on molecular interactions, but such data can be derived from the molecular reactions and complexes annotated.

Two molecules are reported as interactors, if:
1. they are components of the same complex/polymer
2. they are inputs of the same reaction
3. one of them is the catalyst of a reaction and the other one is an input of that reaction.

## Instalation
bla bla bla

## Methods
This is a detailed guide about how interactions are inferred in Reactome.

### Complexes
Every component in a complex is likely to physically interact with each other. However, this probability decreases as the number of components increases. A limit in the number of components can be applied in order to not export these interactions.

```
+ Complex1
|   - EWAS1
|   - EWAS2
|   - SimpleEntity1
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Complex1|EWAS1|EWAS2|Physical
Complex1|EWAS1|SimpleEntity1|Physical
Complex1|EWAS2|SimpleEntity1|Physical

Interactions between simple molecules are not exported.
```
+ Complex1
|   - EWAS1
|   - SimpleEntity1
|   - SimpleEntity2
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Complex1|EWAS1|SimpleEntity1|Physical
Complex1|EWAS1|SimpleEntity2|Physical

If an element in the complex is present more than one time (stoichiometry > 1) then an interaction with itself is added. We call this molecules olygomers.
```
+ Complex1
|   - 2 x EWAS1
|   - SimpleEntity1
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Complex1|0 x EWAS1|2 x EWAS1|Physical
Complex1|2 x EWAS1|SimpleEntity1|Physical

Components of a complex can be other complexes. In that case, each subcomplex is divided in its components.

```
+ Complex1
|   + Complex2
|   |   - EWAS1
|   |   - EWAS2
|   - SimpleEntity1
```

Context | Interactor A | Interactor B|Type
---|---|---|---
Complex2|EWAS1|EWAS2|Physical
Complex1|EWAS1|SimpleEntity1|Physical
Complex1|EWAS2|SimpleEntity1|Physical

Notice that the context for the interaction between EWAS1 and EWAS2 is the subcomplex _Complex2_. We always use the most specific context for an interaction.

Members of entity sets do not interact with each other, as elements in entity sets are interchangeable. Candidates are not taken into account.
```
+ Complex1
|   + EntitySet1
|   |   o EWAS1
|   |   o EWAS2
|   - SimpleEntity1
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Complex1|EWAS1|SimpleEntity1|Physical
Complex1|EWAS2|SimpleEntity1|Physical


[//]: # (If a complex is identified in ComplexPortal, then it is not divided into its components, as it can interact as a unit)

The same interaction can happen in different contexts, when they are not subcomponents of each other.

```
+ Complex1
|   - EWAS1
|   - EWAS2
+ Complex2
|   - EWAS1
|   - EWAS2
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Complex1|EWAS1|SimpleEntity1|Physical
Complex1|EWAS2|SimpleEntity1|Physical
Complex2|EWAS1|SimpleEntity1|Physical
Complex2|EWAS2|SimpleEntity1|Physical


### Polymers
One interaction is inferred from each polymer: the repeated unit with itself. The stoichiometry for both interators is 0.
```
+ Polymer1
|   - EWAS1
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Polymer1|0 x EWAS1|0 x EWAS1|Physical

Of course, if the repeated unit of a polymer is a complex or an entity set, it must be divided.
```
+ Polymer1
|   - Complex1
|   |   - EWAS1
|   |   - SimpleEntity1
+ Polymer2
|   - EntitySet1
|   |   o EWAS2
|   |   o SimpleEntity2
```
Context | Interactor A | Interactor B|Type
---|---|---|---
Polymer1|0 x EWAS1|0 x EWAS1|Physical
Complex1|0 x EWAS1|0 x SimpleEntity|Physical
Polymer1|0 x SimpleEntity1|0 x SimpleEntity1|Physical
Polymer2|0 x EWAS2|0 x EWAS2|Physical
Polymer2|0 x EWAS2|0 x SimpleEntity2|Physical

[//]: # (Polymer2 is tricky, as the interaction between EWAS2 and SimpleEntity2 is exporter, to avoid that, the correct structure should be an EntitySet with 2 different polymers)


### Reactions
Two types of interactions can be inferred from every reaction:
1. The input elements physically interact with each other.
2. The catalyst chemically interacts with every input.

Input interactions are similar to complexes:
```
+ Reaction1
|   i PhysicalEntity1
|   i 4 x PhysicalEntity2
|   i PhysicalEntity3
```
Context | Interactor A | Interactor B|Type
--- | --- | --- | ---
Reaction1|PhysicalEntity1|4 x PhysicalEntity2|Physical
Reaction1|PhysicalEntity1|PhysicalEntity3|Physical
Reaction1|4 x PhysicalEntity2|PhysicalEntity3|Physical
Reaction1|0 x PhysicalEntity2|4 x PhysicalEntity2|Physical

Catalyst's interactions are chemical.
```
+ Reaction1
|   i PhysicalEntity1
|   c PhysicalEntity2
```
Context | Interactor A | Interactor B | Type
--- | --- | --- | ---
Reaction1|PhysicalEntity1|PhysicalEntity2|Chemical

The molecule that acts as the catalyst is the active unit of the catalyst activity, if, and only if, one active unit is specified. If zero or more than one active units are specified, then the physical entity is used.

The input must be formed by one relevant molecule (protein, complex, polymer) and zero or more small molecules (cofactors). If the input is a set (with or without cofactors), every member of the input is interacted with the catalyst. If the input is just one small molecule, then it is used as input.

The type of this interaction depends on the GO-Molecular-function term associated with the CatalystActivity.

BlackBoxEvents are ignored.

