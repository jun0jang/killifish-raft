#!/bin/sh

args=$@

echo $args

echo $args | xargs ./scripts/ktlint -F
