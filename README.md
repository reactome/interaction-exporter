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

Interactions are not oriented: if _a_ interacts with _b_, then _b_ interacts with _a_, so only one interaction is exported. However, duplicated interaction can appear since uniqueness is only warranted at the level of Reactome stable identifiers. If two Reactome objects with different stable identifiers point to the same protein, it is not considered as duplicated.

## Installation
Download the source code
```
git clone https://github.com/reactome/interaction-exporter.git
```
Use maven to create the executable
```
mvn clean package -f pom.xml
```
A target/interaction-exporter-with-dependencies.jar will be created.
## Usage
```
java -jar interaction-exporter-wit-dependencies.jar [--help] 
	(-h|--host) <host> (-b|--port) <port> (-u|--user) <user> (-p|--password) <password>
	(-o|--output) <output>
	[(-m|--maxUnitSize) <maxUnitSize>] 
	[(-s|--species) <species>] [(-O|--object) <object>]
	[(-t|--simpleEntitiesPolicy)  <simpleEntitiesPolicy>]
	[(-v|--verbose)[:<verbose>]]

```
### Parameters
name|flag|description
---|---|---
help | -\-help | prints the help
host | -h, -\-host | The neo4j host (default: localhost)
port | -b, -\-port | The neo4j port (default: 7474)
user | -u, -\-user | The neo4j user (default: neo4j)
password | -p, -\-password | The neo4j password (default: neo4j)
max unit size |-m, -\-maxUnitSize | The maximum size of complexes/sets from which interactions are considered significant. (default: 4)
species | -s, -\-species |1 or more species from which the interactions will be fetched. ALL to export all of the species (default: Homo sapiens)
object | -O, -\-object | 1 or more objects to export interactions under these objects, species will be ignored.
simple entities policy | -t, -\-simpleEntitiesPolicy | Set if simple entities are exported as well: ALL, NONE or NON\_TRIVIAL. (default: NON\_TRIVIAL)
output prefix | -o, -\-output | Prefix of the output files
verbose | -v, -\-verbose | Requests verbose output

#### Note
>The "output prefix" is used to concatenate the provided filename with two extensions: ".psi-mitab.txt" and ".tab-delimited.txt". Therefore, when the output parameter is
```
-o path/to/folder/filename
```
>There will be two files generated in the folder: "filename.psi-mitab.txt" and "filename.tab-delimited.txt".

## Methods
To see a detailed explanation of how we extract interactions from Reactome see the wiki: https://github.com/reactome/interaction-exporter/wiki/Methods

## PSI-MITAB output
Results are exported using PSI-MITAB version 27 (https://github.com/HUPO-PSI/miTab/blob/master/PSI-MITAB27Format.md). 

column | name | multiplicity | value, as (db):(id):(text) | source
--- | --- | --- | --- | ---
1, 2 | interactor unique identifier | 1 | (ChEBI, reactome, uniprotkb):(\*) | uniprotkb for proteins, ChEBI for small molecules, reactome as default
3, 4 | interactor alternative identifiers | 0..* | (\*):(\*) | any other identifier
5, 6 | interactor aliases | 0..* |(reactome):(\*):(name) | entity.names if !name.contains("\n") && !name.contains(":")
7 | interaction detection methods | 1 |(psi-mi):(MI:0364):(inferred by curator) |constant
8 | first author | 1..* | (\*) | context.literatureReferences.author, by default *Fabregat et al. (2015)*
9 | publication identifier | 1..* | (\*) | context.literatureReferences.pubmedIdentifier. By default *pubmed:24243840*
10, 11 | interactor taxonomy | 1..* | (taxid):(\*) |interactor.species
12 | Interaction types | 1 |(psi-mi):(\*):(\*) | physical for complex/polymer/inputs, reaction.catalyst.activity for reactions
13 | Source databases | 1 |(psi-mi):(MI:0467):(reactome) | constant
14 | Interaction identifiers | 1 | (reactome):(\*)| context.stId
15 | Confidence score | 1 |(reactome-score):(\*) | 0.4 if context is inferred, otherwise 0.5
16 | Complex expansion | 0..1 | (psi-mi):(MI:1061):(matrix expansion) | matrix expansion for complex/polymer/input. empty for catalyst interactions
17, 18 | Biological roles | 1 | (psi-mi):(MI:0499,  MI:0501,  MI:0502):(enzyme,  enzyme target,  unspecified role) | unspecified for complex/polymer/inputs. enzyme/enzyme target for catalyst/input pairs
19, 20 | Experimental roles | 1 |(psi-mi):(MI:0499):(unspecified role) | constant
21, 22 | Interactors type | 0..1 | (psi-mi):(\*):(\*) | biopolymer,  complex,  desoxyribonucleic acid,  protein,  ribonucleic acid,  small molecule
23, 24 | interactors Xref | 1..* | (go):(\*):(\*) |interactors.compartments
25 | interaction Xref | 1..* | (go):(\*):(\*) |context.compartments, context.catalyst.activity
26, 27 | interactors annotations | 0 | - | empty
28 | interaction annotations | 0 | - | empty
29 | interaction taxonomy | 1 | (taxid):(\*):(\*) |context.species
30 | interaction parameters | 0 | - | empty
31 | Creation date | 0..1 | (\*) | context.created
32 | Update date | 0..1 | (\*) | context.modified
33, 34 | interactors checksum | 0 | - | empty
35 | interaction checksum | 0 | - | empty
36 | negative | 1 | false | false
37, 38 | interactors features | 0..* | (\*):(\*):(\*) |TranslationalModification in interactor.hasModifiedResidue
39, 40 | interactors stoichiometry | 1 | (\*) | (n, n) for complex/input. (0, n) for olygomer. (0, 0) for polymer. (1, n) for catalyst/input
41, 42 | interactor identification method | 1 | (psi-mi):(MI:0364):(inferred by curator) | constant

### TSV format
A lighter format with minimum information. We highly encourage using PSI-MITAB standard format.

column | name | multiplicity | value | source
--- | --- | --- | --- | ---
1, 4 | interactor uniprot id | 1..* | (ChEBI, reactome, uniprotkb):(\*) | uniprotkb for proteins, ChEBI for small molecules, reactome as default
2, 5 | interactor esembl id | 0..* | (ENSEMBL):(\*) | identifiers matching ENSG#######
3, 6 | interactor EntrezGene | 0..* | (EntrezGene):(\*) | identifiers matching EntrezGene:#####
7 |  Interaction type | 0..1 | (complex,  reaction) | context.class (empty if not complex or reaction)
8 | Interaction context | 1 | (\*) | context.stId
9 | Pubmed references | 0..* | (\*) | context.literatureReferences









