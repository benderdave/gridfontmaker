# GridfontMaker

GridfontMaker is an editor for creating and modifying **gridfonts**, as
described in [*Fluid Concepts and Creative Analogies*](https://en.wikipedia.org/wiki/Fluid_Concepts_and_Creative_Analogies), 
by Douglas Hofstadter and the members of the [Fluid Analogies Research
Group](http://cogsci.indiana.edu) 
(FARG) at Indiana University.

Gridfonts are stripped-down fonts designed under extreme
constraints. The 26 lowercase English letters make up a complete gridfont
&emdash no
capitals, punctuation, or numerals &emdash
and each is created on a 3x7 grid of anchors. Only neighboring anchors can be
connected (diagonals are allowed), and each stroke is a simple line, without
decorations or variations in thickness. 

<!-- ![Image explaining gridfont domain]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/gridfont-defn.png)
<img align="center" src="https://raw.githubusercontent.com/benderdave/gridfontmaker/master/gridfont-defn.png" alt="Image explaining the gridfont domain"> -->
<div style="text-align:center"><img src ="https://raw.githubusercontent.com/benderdave/gridfontmaker/master/gridfont-defn.png" /></div>

Even with these radical
restrictions many beautiful fonts have been created. The domain's severly restricted nature
forces a font designer quickly into radical experimentation and exploration.

![Sampling of gridfonts]
(https://raw.githubusercontent.com/benderdave/gridfontmaker/master/benzene-etc.png)

The gridfont domain is a micro-domain for studying the cognitive processes
that underlie thinking. See FARG's [gridfont page](http://cogsci.indiana.edu/gridfonts.html) for work by John 
Rehling (and Gary McGraw). That said, we've found that designing gridfonts is a
uniquely creative activity that stands on its own. 

GridfontMaker is provided to
help anyone interested in gridfonts create and share them.

# Building and Running

Right now the only way to build GridfontMaker is on a unix-like system with
*make*, *java*, and *scala*. 

* Clone the source
* Make sure you have a recent version of both java and scala
* Locate, download, and put the following dependencies in ./lib3rd
  * genson-1.3.jar
  * json-simple-1.1.1.jar
  * genson-scala_2.11-1.3.jar
* make; make run

Obviously this ins't ideal. When we've debugged the problems with wrapped
versions for linux/mac/windows I'll add links to them here. (No, I'm not going
to switch to sbt.)

# Problems

If you have any problems, questions, bugreports, requests, or comments, feel free to email me at
dcbender@indiana.edu. (However, I can't promise I'll be quick to respond, as
I'm working on my dissertation.) If you create any interesting fonts, please
share them!
