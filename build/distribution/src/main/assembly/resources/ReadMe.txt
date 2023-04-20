Welcome to Timefold
======================

Run the quickstarts
-------------------

On Linux, macOS or Cygwin:
  ./runQuickstarts.sh
On Windows:
  runQuickstarts.bat

Run the quickstarts in IDE
----------------------------

Please refer to https://github.com/TimefoldAI/timefold-quickstarts/blob/stable/README.adoc.

Run the examples
----------------

It's easy to run the examples:
On Linux, macOS or Cygwin:
  ./runExamples.sh
On Windows:
  runExamples.bat


Run the examples in IntelliJ
----------------------------

Open menu File, menu item Open project, select "examples/sources/pom.xml".

Open menu Run, menu item Edit Configurations, add a new Application
with main class "ai.timefold.solver.examples.app.TimefoldExamplesApp"
and run that.


Run the examples in Eclipse
---------------------------

Import "examples/sources/pom.xml" as a new project from Maven sources.

Open menu Run, menu item Run configurations, add new Java Application
with main class "ai.timefold.solver.examples.app.TimefoldExamplesApp"
and click button Run.


Read the reference manual
-------------------------

To see the reference_manual, just open:
  https://timefold.ai/docs/
It contains information how to use it on your project (with Maven, Gradle, ...).


Sources
-------

But to build from sources, pull the sources with git:
  https://github.com/TimefoldAI/timefold-solver

Backwards compatibility
-----------------------

Timefold's api packages are backwards compatible.
The impl packages are not, apply the upgrade recipe if you use them:
  https://timefold.ai/docs/
For more specific information, see the first chapter of the reference manual.


Questions?
----------

If you have any questions, visit:
  https://timefold.ai/product/support/
