package org.zxp.esclientrhl.repository;

/**
 * @program: esclientrhl
 * @description:
 * @author: X-Pacific zhang
 * @create: 2020-09-10 11:24
 **/
public class GeoEntity {
    private double lat;
    private double lon;

    @Override
    public String toString() {
        return "GeoEntity{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public GeoEntity(double lat, double lon) {

        this.lat = lat;
        this.lon = lon;
    }

}
