BUILD_ROOT=./_build
BUILD_CLASSDIR=$(BUILD_ROOT)/classes

LIB3RD=./lib3rd
CLASS_PATH=$(BUILD_CLASSDIR):$(LIB3RD)/genson-scala_2.11-1.3.jar:$(LIB3RD)/genson-1.3.jar
SCALA_FLAGS=-d $(BUILD_CLASSDIR) -classpath .:$(CLASS_PATH) -Xfatal-warnings\
 -explaintypes -Xprint-types -feature -deprecation
COMPILE_SCALA=@fsc -reset && ./runfsc $(SCALA_FLAGS)

TARGET_FILE=$(BUILD_CLASSDIR)/farg/GridfontMaker.class

SOURCE_FILES=$(wildcard *.scala)

.PHONY: all clean run

all: gridfontmaker.jar gridfontmaker

run:
	java -jar gridfontmaker.jar

clean:
	rm -rf $(BUILD_ROOT) runscala runfsc gridfontmaker gridfontmaker.jar

$(TARGET_FILE): runfsc $(BUILD_CLASSDIR) $(SOURCE_FILES)
	$(COMPILE_SCALA) $(SOURCE_FILES)

gridfontmaker.jar: $(TARGET_FILE)
	cd $(BUILD_ROOT)/classes; jar -cfm ../../gridfontmaker.jar ../../Manifest.mf *

runscala: Makefile
	echo "#! /usr/bin/env bash" > runscala
	echo "# runner for Scala programs" >> runscala
	echo "# generated" `date` >> runscala
	echo "scala $(SCALA_FLAGS)" '"$$@"' >> runscala
	chmod a+x runscala

runfsc:
	echo "#! /usr/bin/env bash" > runfsc
	echo "# fast Scala compiler runner" >> runfsc
	echo "# generated" `date` >> runfsc
	echo "echo fsc" '"$$@"' >> runfsc
	echo "fsc" '"$$@"' >> runfsc
	chmod a+x runfsc

gridfontmaker: $(TARGET_FILE) runscala Makefile
	echo "#! /usr/bin/env bash" > gridfontmaker
	echo ./runscala farg.GridfontMaker '"$$@"' >> gridfontmaker
	chmod a+x gridfontmaker

$(BUILD_CLASSDIR):
	mkdir -p $(BUILD_CLASSDIR)
