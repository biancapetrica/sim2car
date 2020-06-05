package model;

import application.routing.RoutingApplicationParameters;
import controller.network.NetworkInterface;
import controller.network.NetworkType;
import model.OSMgraph.Node;
import model.mobility.MobilityEngine;
import utils.tracestool.Utils;
import model.GeoTrafficLightMaster;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static controller.network.NetworkType.Net_WiFi;

/***
 * The class represents the server for master traffic lights of all intersection.
 * All masters are centralizes in this class.
 * It's job is to create a dependency between master traffic lights and synchronize data
 * between them.
 * @author Bianca
 *
 */
public class GeoTrafficLightMastersServer extends Entity{
    /** Reference to mobility */
    private MobilityEngine mobility;

    public List<GeoTrafficLightMaster> masterTrafficLights;
    public TreeMap<Long, List<GeoTrafficLightMaster>> masterTrafficLightsNeighbours;

    public GeoTrafficLightMastersServer(long id) {
        super(id);
        this.mobility = MobilityEngine.getInstance();
        this.masterTrafficLights = new ArrayList<GeoTrafficLightMaster>();
        this.masterTrafficLightsNeighbours = new TreeMap<Long, List<GeoTrafficLightMaster>>();
    }

    @Override
    public List<GeoTrafficLightMaster> getMasterTrafficLights() {
        return masterTrafficLights;
    }

    public void setMasterTrafficLights(List<GeoTrafficLightMaster> masterTrafficLights) {
        this.masterTrafficLights = masterTrafficLights;
    }

    public TreeMap<Long, List<GeoTrafficLightMaster>> getMasterTrafficLightsNeighbours() {
        return masterTrafficLightsNeighbours;
    }

    public void setMasterTrafficLightsNeighbours(TreeMap<Long, List<GeoTrafficLightMaster>> masterTrafficLightsNeighbours) {
        this.masterTrafficLightsNeighbours = masterTrafficLightsNeighbours;
    }

    public void updateNeighbours(GeoTrafficLightMaster trafficLight) {
        ArrayList<GeoTrafficLightMaster> mtl = (ArrayList<GeoTrafficLightMaster>) masterTrafficLights;
        Node nodeSourceMaster = trafficLight.getNode();
        long waySourceMaster = nodeSourceMaster.wayId;
        ArrayList<GeoTrafficLightMaster> neighbours;

        if (masterTrafficLightsNeighbours.containsKey(trafficLight.getId())) {
            neighbours = (ArrayList<GeoTrafficLightMaster>) masterTrafficLightsNeighbours.get(trafficLight.getId());
        } else {
            neighbours = new ArrayList<GeoTrafficLightMaster>();
        }

        double dist = 0;
        for (int i = 0; i < mtl.size(); i++) {
            GeoTrafficLightMaster currentMaster = mtl.get(i);
            Node nodeCurrentMaster = currentMaster.getNode();
            long wayCurrentMaster = nodeCurrentMaster.wayId;

            //Get all ways from this intersection (where this master traffic light is situated)
            // Collect all neighbours streets(ways) from current intersection
            List<Long> thisIntersection = mobility.streetsGraph.get(wayCurrentMaster).neighs.get(nodeCurrentMaster.id);
            long link = 0;
            //Get all ways from the first intersection (traffic light)
            // Collect all neighbours streets(ways) from source intersection from which the sync request came
            // and search for a link between these 2 intersections
            for (long wayFromFirstIntersection : mobility.streetsGraph.get(waySourceMaster).neighs.get(nodeSourceMaster.id)) {
                if (thisIntersection.contains(wayFromFirstIntersection)) {
                    link = wayFromFirstIntersection;
                    break;
                }
            }
            dist = Utils.distance(trafficLight.getCurrentPos().lat, trafficLight.getCurrentPos().lon,
                    currentMaster.getCurrentPos().lat, currentMaster.getCurrentPos().lon);
            if (link != 0 && dist < RoutingApplicationParameters.distMax && currentMaster.getId() != trafficLight.getId()) {
                neighbours.add(currentMaster);
            }
        }

        masterTrafficLightsNeighbours.put(trafficLight.getId(), neighbours);
        for (GeoTrafficLightMaster master : neighbours) {
            ArrayList masterNeighbours;
            if (masterTrafficLightsNeighbours.containsKey(master.getId())) {
                masterNeighbours = (ArrayList<GeoTrafficLightMaster>) masterTrafficLightsNeighbours.get(master.getId());
            } else {
                masterNeighbours = new ArrayList<GeoTrafficLightMaster>();
            }
            masterNeighbours.add(trafficLight);
            masterTrafficLightsNeighbours.put(master.getId(), masterNeighbours);
        }
    }

    public List<NetworkInterface> getClosestsTrafficLightMastersNetworks(GeoTrafficLightMaster master, NetworkType type) {
        ArrayList<GeoTrafficLightMaster> neighbours = (ArrayList<GeoTrafficLightMaster>) masterTrafficLightsNeighbours.get(master.getId());
        List<NetworkInterface> ret = new ArrayList<NetworkInterface>();

        for (GeoTrafficLightMaster masterNeighbour : neighbours) {
            ret.add(masterNeighbour.getNetworkInterface(type));
        }
        return ret;
    }
}
