PWD := $(shell pwd)
TOOL_ARTIFACT := com.github.liquidz/clj-error-checker
TOOL_NAME := error-checker

PLATFORM := $(shell uname -s | tr '[:upper:]' '[:lower:]')

GRAAL_ROOT ?= ./.graalvm
GRAAL_VERSION ?= 21.3.0
GRAAL_HOME ?= $(GRAAL_ROOT)/graalvm-ce-java11-$(GRAAL_VERSION)
GRAAL_ARCHIVE := graalvm-ce-java11-$(PLATFORM)-amd64-$(GRAAL_VERSION).tar.gz

ifeq ($(PLATFORM),darwin)
	GRAAL_HOME := $(GRAAL_HOME)/Contents/Home
	GRAAL_EXTRA_OPTION :=
else
	GRAAL_EXTRA_OPTION := "--static"
endif

.PHONY: tool-list
tool-list:
	clojure -Ttools list

.PHONY: tool-install
tool-install:
	clojure -Ttools install ${TOOL_ARTIFACT} '{:local/root "${PWD}"}' :as ${TOOL_NAME}

.PHONY: tool-remove
tool-remove:
	clojure -Ttools remove :tool ${TOOL_NAME}

$(GRAAL_ROOT)/fetch/$(GRAAL_ARCHIVE):
	@mkdir -p $(GRAAL_ROOT)/fetch
	curl --location --output $@ https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$(GRAAL_VERSION)/$(GRAAL_ARCHIVE)

$(GRAAL_HOME): $(GRAAL_ROOT)/fetch/$(GRAAL_ARCHIVE)
	tar -xz -C $(GRAAL_ROOT) -f $<

$(GRAAL_HOME)/bin/native-image: $(GRAAL_HOME)
	$(GRAAL_HOME)/bin/gu install native-image

.PHONY: graalvm
graalvm: $(GRAAL_HOME)/bin/native-image

.PHONY: uberjar
uberjar: clean
	clojure -T:build uberjar

.PHONY: native-image
native-image: graalvm uberjar
	$(GRAAL_HOME)/bin/native-image \
		-jar target/error-checker.jar \
		-H:Name=clj-error-checker \
		-H:+ReportExceptionStackTraces \
		-J-Dclojure.spec.skip-macros=true \
		-J-Dclojure.compiler.direct-linking=true \
		--initialize-at-build-time  \
		--report-unsupported-elements-at-runtime \
		-H:Log=registerResource: \
		--verbose \
		--no-fallback \
		--no-server \
		$(GRAAL_EXTRA_OPTION) \
		"-J-Xmx3g"

.PHONY: lint
lint:
	cljstyle check
	clj-kondo --lint src

.PHONY: clean
clean:
	rm -rf .cpcache target
