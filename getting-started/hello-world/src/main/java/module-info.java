module org.acme.schooltimetabling {

    requires ai.timefold.solver.core;
    requires ai.timefold.solver.console;
    requires org.slf4j;
    requires org.jline;

    exports org.acme.schooltimetabling.domain;
    exports org.acme.schooltimetabling.solver;

    opens org.acme.schooltimetabling.domain to ai.timefold.solver.core;
    opens org.acme.schooltimetabling.solver to ai.timefold.solver.core;

}
