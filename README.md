# GridfontMaker

GridfontMaker is an editor for creating and modifying **gridfonts**, as
described in [*Fluid Concepts and Creative Analogies*](https://en.wikipedia.org/wiki/Fluid_Concepts_and_Creative_Analogies), 
by Douglas Hofstadter and the members of the [Fluid Analogies Research
Group](http://cogsci.indiana.edu) 
(FARG) at Indiana University.

Gridfonts are stripped-down typefaces designed under extreme
constraints. The 26 lowercase English letters make up a complete gridfont
&mdash; no
capitals, punctuation, or numerals are included &mdash;
and each is created on a 3x7 grid of anchors. Only neighboring anchors can be
connected (45-degree diagonals are allowed), and each stroke is a simple line, without
decorations or variations in thickness. 

![Image explaining gridfont domain]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/images/gridfont-defn.png)

Even under these radical
restrictions many interesting fonts have been created. The domain's severely restricted nature
forces a font designer quickly into radical experimentation and exploration.
The following are among the hundreds of gridfonts created by Douglas Hofstadter.

![Sampling of gridfonts]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/images/benzene-etc.png)

The gridfont domain is a micro-domain intended for studying the cognitive process
that underlies most thinking: analogy. See FARG's [gridfont page](http://cogsci.indiana.edu/gridfonts.html) for work by John 
Rehling and Gary McGraw. That said, we've found that designing gridfonts is a
uniquely creative activity that stands on its own. 

GridfontMaker is provided to
help anyone interested in gridfonts create and share them.

# Running

Right now you can run GridfontMaker (without compiling it yourself) in two ways,
from a wrapped application
specific to your operating system, or straight from the **jar** file.

## Pre-built apps

Download the appropriate link for your OS and run it as you
would a normal program. This is the easiest option because you don't have to
install anything else.

[MacOS](https://www.dropbox.com/s/rizelu5tle1o9yw/GridfontMaker-macos64-offline.dmg?dl=0)

[Linux](https://www.dropbox.com/s/b2d8055r07yc7pp/GridfontMaker-linux64-offline.tar?dl=0)

[Windows 64-bit](https://www.dropbox.com/s/hgnhzg3t5kod4dc/GridfontMaker-windows64-offline.exe?dl=0)

[Windows 32-bit](https://www.dropbox.com/s/xilu1rqr8ve82w3/GridfontMaker-windows32-offline.exe?dl=0)

Please let me know if any of these don't work for you. I only have one or two 
sytems to test on, and no installations of windows.

## Using java

If you have java installed you can run GridfontMaker without compiling
anything (but you have to install dependencies).

* Clone the source
* Make sure you have a recent version of java
* Locate and download the following dependencies:
  * genson-1.3.jar (put in ./lib3rd)
  * genson-scala_2.11-1.3.jar (put in ./lib3rd)
  * scala-library.jar (from scala-2.11; put in ./libscala)
  * scala-reflect.jar (from scala-2.11; put in ./libscala)
* Run with "java -jar gridfontmaker.jar" (on some systems you can just
  double-click on the jar file.)

# Building and running from source

Right now the only way to build GridfontMaker is on a unix-like system with
*make*, *java*, and *scala* (and you have to install dependencies).

* Clone the source
* Make sure you have a recent version of both java and scala
* Locate, download, and put the following dependencies in ./lib3rd
  * genson-1.3.jar
  * genson-scala_2.11-1.3.jar
* Build with "make"
* Run with "make run"

Example fonts are included in the "fonts" directory.

# Roadmap

Features I'm considering adding:

* export to true-type format
* inkscape plugin to render text in a gridfont
* support variable letter widths in gridfont text area
* show another gridfont in a separate window for comparison

# Problems

If you have any problems, questions, bug-reports, requests, or comments, feel free to email me at
dcbender@indiana.edu. (However, I can't promise I'll be quick to respond, as
I'm working on my dissertation.) If you create any interesting fonts, please
share them!
