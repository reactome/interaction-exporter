#!/bin/bash
java -Xmx4G -jar ../target/interaction-exporter-jar-with-dependencies.jar -H localhost -P 7474 -U neo4j -W reactome $*

