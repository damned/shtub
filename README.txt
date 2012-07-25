shtub - http stubbing for tests

testing shtub proxy:

-- testing system using shtub to stub http dependency --

in Graphviz dot format, SUT == System Under Test:

  digraph testing_system_with_stubbed_http_dependency {
     test->shtub [label="0: in-process setup"];
     test->SUT [label="1: action"];
     SUT->shtub [label="http"];
     shtub->SUT;
     SUT->test [label="2: assert"];
  }

render using:

  http://sandbox.kidstrythisathome.com/erdos/index.html

or Graphviz itself:

  http://www.graphviz.org/

