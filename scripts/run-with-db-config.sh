#!/bin/bash
java -Xmx4G -jar ../target/interaction-exporter-jar-with-dependencies.jar -h localhost -b 7474 -u neo4j -p reactome $*

