package com.example.trainreservationsystem.services;

import com.example.trainreservationsystem.models.Route;
import com.example.trainreservationsystem.models.RouteSegment;
import com.example.trainreservationsystem.repositories.RouteRepository;

import java.util.List;

public class RouteService {

    private final RouteRepository routeRepository = new RouteRepository();

    public Route createRoute(String name, List<RouteSegment> segments) throws Exception {
        Route route = new Route();
        route.setName(name);
        route = routeRepository.addRoute(route);

        for (RouteSegment segment : segments) {
            routeRepository.addRouteSegment(segment, route.getId());
        }

        route.setSegments(segments);
        return route;
    }

    public List<Route> getAllRoutes() throws Exception {
        return routeRepository.getAllRoutes();
    }

    public Route getRouteById(int id) throws Exception {
        return routeRepository.getRouteById(id);
    }

    public double calculateTotalDistance(int routeId) throws Exception {
        Route route = routeRepository.getRouteById(routeId);
        if (route != null) {
            return route.totalDistance();
        }
        return 0.0;
    }

    public double calculateTotalPrice(int routeId) throws Exception {
        Route route = routeRepository.getRouteById(routeId);
        if (route != null) {
            return route.totalPrice();
        }
        return 0.0;
    }
}
