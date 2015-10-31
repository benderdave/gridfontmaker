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
decorations or variation in thickness. 

![Image explaining gridfont domain]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/images/gridfont-defn.png)

Even under these radical
restrictions many interesting fonts have been created. The domain's severely limited nature
propels a font designer quickly into deep experimentation and widespread exploration.
The following are among the hundreds of gridfonts created by Douglas Hofstadter.

![Sampling of gridfonts]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/images/benzene-etc.png)

The gridfont micro-domain was created for studying *analogy*, the cognitive process
that underlies most thinking. (See FARG's [gridfont page](http://cogsci.indiana.edu/gridfonts.html) for work in the gridfont domain by John 
Rehling and Gary McGraw.) Even so, we've found that designing gridfonts can be a
creative activity that stands on its own, independent of what the gridfont domain might 
have to say about Cognitive Science. Beginners can create a 
new gridfont in just a few minutes without any prior experience in typeface 
design.

GridfontMaker is provided to
help anyone interested in gridfonts create and share them.

# Running

You can run GridfontMaker (without compiling it yourself) in two ways,
from a wrapped application
specific to your operating system, or straight from the **jar** file.

## Pre-built apps

Download the appropriate link for your OS and install/run it as you
would a normal program. This is the easiest option because you don't have to
install anything else.

&nbsp;&nbsp;&nbsp;&nbsp; [MacOS](https://www.dropbox.com/s/rizelu5tle1o9yw/GridfontMaker-macos64-offline.dmg?dl=0)

&nbsp;&nbsp;&nbsp;&nbsp; [Linux 64-bit](https://www.dropbox.com/s/b2d8055r07yc7pp/GridfontMaker-linux64-offline.tar?dl=0)

&nbsp;&nbsp;&nbsp;&nbsp; [Linux 32-bit](https://www.dropbox.com/s/eta3lsjpqtl8gd7/GridfontMaker-linux32-offline.tar?dl=0)

&nbsp;&nbsp;&nbsp;&nbsp; [Windows 64-bit](https://www.dropbox.com/s/hgnhzg3t5kod4dc/GridfontMaker-windows64-offline.exe?dl=0)

&nbsp;&nbsp;&nbsp;&nbsp; [Windows 32-bit](https://www.dropbox.com/s/xilu1rqr8ve82w3/GridfontMaker-windows32-offline.exe?dl=0)

Please let me know if any of these don't work for you. I only have linux,
windows xp, and older mac sytems to test on.

## Using java

If you have java installed you can run GridfontMaker without compiling
anything.

* Clone the source
* Make sure you have a recent version of java
* Run with "java -jar gridfontmaker.jar".

# Building and running from source

If you'd prefer to build GridfontMaker you need a unix-like system with
*make*, *java*, and *scala*.

* Clone the source
* Make sure you have a recent version of both java and scala
* Build with "make"
* Run with "./gridfontmaker"

Example fonts are included in the "fonts" directory.

# Future

Features I'm considering adding:

* export to true-type font format (ttf)

# Problems

If you have any problems, questions, bug-reports, requests, or comments, feel free to email me at
dcbender@indiana.edu. (However, I can't promise I'll be quick to respond, as
I'm working on my dissertation.) If you create any interesting fonts, please
share them!

# Legal

GridfontMaker is written in [scala](http://www.scala-lang.org) and uses the [Genson](http://owlike.github.io/genson/)
library for (de)serializing. License notices for these projects are included.
* [Genson](https://raw.githubusercontent.com/benderdave/gridfontmaker/master/GENSON_LICENSE)
* [Scala](https://raw.githubusercontent.com/benderdave/gridfontmaker/master/SCALA_LICENSE)
