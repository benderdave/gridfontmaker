# GridfontMaker

GridfontMaker is an editor for creating and modifying **gridfonts**, as
described in [*Fluid Concepts and Creative Analogies*](https://en.wikipedia.org/wiki/Fluid_Concepts_and_Creative_Analogies), 
by Douglas Hofstadter and the members of the [Fluid Analogies Research
Group](http://cogsci.indiana.edu) 
(FARG) at Indiana University.

Gridfonts are stripped-down fonts designed under extreme
constraints. The 26 lowercase English letters make up a complete gridfont
&mdash; no
capitals, punctuation, or numerals &mdash;
and each is created on a 3x7 grid of anchors. Only neighboring anchors can be
connected (diagonals are allowed), and each stroke is a simple line, without
decorations or variations in thickness. 

![Image explaining gridfont domain]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/gridfont-defn.png)

Even with these radical
restrictions many beautiful fonts have been created. The domain's severly restricted nature
forces a font designer quickly into radical experimentation and exploration.
The following were created by Douglas Hofstadter.

![Sampling of gridfonts]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/benzene-etc.png)

The gridfont domain is a micro-domain intended for studying the cognitive process
that underlies most thinking: analogy. See FARG's [gridfont page](http://cogsci.indiana.edu/gridfonts.html) for work by John 
Rehling (and Gary McGraw). That said, we've found that designing gridfonts is a
uniquely creative activity that stands on its own. 

GridfontMaker is provided to
help anyone interested in gridfonts create and share them.

# Running

Right now you can run GridfontMaker (without comiliing it yourself) in two ways,
from a wrapped application
specific to your operating system, or straight from the **jar** file.

## Pre-built apps

Download the appropriate link for your OS, install the app and run it as you
would a normal program. This is the easiest option because you don't have to
install anything else.

fixme: add links

Please let me know if any of these don't work for you. I only have one or two 
sytems to test on.

## Using java

If you have java installed you can run GridfontMaker without compiling
anything.

* Clone the source
* Make sure you have a recent version of java
* Locate, download, and put the following dependencies in ./lib3rd
  * genson-1.3.jar
  * json-simple-1.1.1.jar
  * genson-scala_2.11-1.3.jar
* Run with "java -jar gridfontmaker.jar" (on some systems you can just
  double-click on the jar file.)

# Building and running from source

Right now the only way to build GridfontMaker is on a unix-like system with
*make*, *java*, and *scala*. 

* Clone the source
* Make sure you have a recent version of both java and scala
* Locate, download, and put the following dependencies in ./lib3rd
  * genson-1.3.jar
  * json-simple-1.1.1.jar
  * genson-scala_2.11-1.3.jar
* Build with "make"
* Run with "make run"

Example fonts are included in the "fonts" directory.

# Problems

If you have any problems, questions, bugreports, requests, or comments, feel free to email me at
dcbender@indiana.edu. (However, I can't promise I'll be quick to respond, as
I'm working on my dissertation.) If you create any interesting fonts, please
share them!
