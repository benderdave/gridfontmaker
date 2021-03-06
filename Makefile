MACH=$(shell uname)

BUILD_ROOT=./_build
BUILD_CLASSDIR=$(BUILD_ROOT)/classes
DISTDIR=./dist
JWRAPPER_BUILDDIR=./jwrapper/JWrapperBuild

LIB3RD=./lib3rd
LIBSCALA=./libscala
DEPS=$(LIB3RD)/genson-scala_2.11-1.3.jar:$(LIB3RD)/genson-1.3.jar
CLASS_PATH=$(BUILD_CLASSDIR):$(DEPS)
SCALA_FLAGS=-d $(BUILD_CLASSDIR) -classpath .:$(CLASS_PATH) -Xfatal-warnings\
 -explaintypes -Xprint-types -feature -deprecation
COMPILE_SCALA=@fsc -reset && ./runfsc $(SCALA_FLAGS)

TARGET_FILE=$(BUILD_CLASSDIR)/farg/GridfontMaker.class

SOURCE_FILES=$(filter-out MacOSXInit.scala,$(wildcard *.scala))
ifeq ($(MACH),Darwin)
  SOURCE_FILES+=MacOSXInit.scala
endif

.PHONY: all clean run tags

all: gridfontmaker.jar gridfontmaker

run:
	java -jar gridfontmaker.jar

clean:
	rm -rf $(BUILD_ROOT) runscala runfsc gridfontmaker gridfontmaker.jar

tags:
	ctags $(SOURCE_FILES)

$(TARGET_FILE): runfsc $(BUILD_CLASSDIR) Makefile $(SOURCE_FILES)
	$(COMPILE_SCALA) $(SOURCE_FILES)

gridfontmaker.jar: $(TARGET_FILE)
	cd $(BUILD_ROOT)/classes; jar -cfm ../../gridfontmaker.jar ../../Manifest.mf *

runscala: Makefile
	echo "#! /usr/bin/env bash" > runscala
	echo "# Scala runner generated" `date` >> runscala
	echo "scala -nc $(SCALA_FLAGS)" '"$$@"' >> runscala
	chmod a+x runscala

runfsc:
	echo "#! /usr/bin/env bash" > runfsc
	echo "# fast Scala compiler runner generated" `date` >> runfsc
	echo "echo fsc" '"$$@"' >> runfsc
	echo "fsc" '"$$@"' >> runfsc
	chmod a+x runfsc

gridfontmaker: $(TARGET_FILE) runscala Makefile
	echo "#! /usr/bin/env bash" > gridfontmaker
	echo ./runscala farg.GridfontMaker '"$$@"' >> gridfontmaker
	chmod a+x gridfontmaker

dist:
	mkdir -p $(DISTDIR)
	cp $(JWRAPPER_BUILDDIR)/GridfontMaker-linux64-offline.tar $(DISTDIR)
	cp $(JWRAPPER_BUILDDIR)/GridfontMaker-macos64-offline.dmg $(DISTDIR)
	cp $(JWRAPPER_BUILDDIR)/GridfontMaker-windows64-offline.exe $(DISTDIR)
.PHONY: dist

$(BUILD_CLASSDIR):
	mkdir -p $(BUILD_CLASSDIR)
