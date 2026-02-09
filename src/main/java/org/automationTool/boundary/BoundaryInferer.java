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
                Set<String> intersection = new HashSet<>(service.entities);
                intersection.retainAll(controller.entities);

                if (!intersection.isEmpty()) {
                    service.controllers.add(controller.controllerName);
                    service.entities.addAll(controller.entities);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                Microservice service = new Microservice();
                service.name = controller.controllerName + "Service";
                service.controllers.add(controller.controllerName);
                service.entities.addAll(controller.entities);
                services.add(service);
            }
        }
        return services;
    }
}
