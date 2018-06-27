#!/bin/bash
java -cp build/lib/jcommander-1.72.jar:build/lib/bcprov-jdk15on-1.54.jar:build/lib/web-push-3.1.0.jar:build/lib/guava-19.0.jar nl.martijndwars.webpush.cli.Cli generate-key
