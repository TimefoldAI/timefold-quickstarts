package ai.timefold.solver.quickstarts.all.rest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import io.quarkus.runtime.Quarkus;

@Path("exit")
public class ExitResource {

    @POST
    public void exit() {
        Quarkus.asyncExit();
    }

}
