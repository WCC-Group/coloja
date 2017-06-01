#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] && [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then

  	openssl aes-256-cbc -K $encrypted_46b4bbc90abb_key -iv $encrypted_46b4bbc90abb_iv -in codesigning.asc.enc -out codesigning.asc -d

    gpg --fast-import cd/signingkey.asc
fi