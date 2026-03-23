package org.automationTool.boundary;

import org.automationTool.model.ControllerInfo;
import org.automationTool.model.Microservice;

import java.util.*;

public class BoundaryInferer {

    public static List<Microservice> infer(Collection<ControllerInfo> controllers) {

        List<Microservice> services = new ArrayList<>();

        for (ControllerInfo controller : controllers) {

            boolean assigned = false;

            for (Microservice service : services) {

                // Check entity overlap
                Set<String> intersection = new HashSet<>(service.getEntities());
                intersection.retainAll(controller.entities);

                if (!intersection.isEmpty()) {
                    service.addController(controller.filePath);
                    service.getEntities().addAll(controller.entities);

                    assigned = true;
                    break;
                }
            }

            if (!assigned) {

                Microservice service =
                        new Microservice(controller.controllerName + "Service");

                service.addController(controller.filePath);
                service.getEntities().addAll(controller.entities);

                services.add(service);
            }
        }

        return services;
    }
}