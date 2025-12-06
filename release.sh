#!/usr/bin/env bash

mvn release:clean release:prepare -P ossrh && \
mvn release:perform -P ossrh
