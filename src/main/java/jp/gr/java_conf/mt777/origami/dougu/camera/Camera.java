package jp.gr.java_conf.mt777.origami.dougu.camera;

//import java.util.*;

import jp.gr.java_conf.mt777.zukei2d.ten.*;
import jp.gr.java_conf.mt777.zukei2d.senbun.*;

public class Camera {//実際の座標と、表示座標の仲立ち

    //int camera_id;//cameraの識別用。名前の変わりに使うだけ

    double camera_position_x, camera_position_y;
    double camera_angle;
    double camera_mirror; //-Either 1.0 or 1.0 Change the sign only on the x-axis // Mirror
    double camera_zoom_x;
    double camera_zoom_y;
    double display_position_x, display_position_y;

    double do2rad, rad2do;
    double camera_rad;
    double sin_rad, cos_rad;


    public Camera() {//コンストラクタ

        //camera_id=0;
        do2rad = 3.14159265 / 180.0;
        rad2do = 180.0 / 3.14159265;
        camera_position_x = 0.0;
        camera_position_y = 0.0;
        camera_angle = 0.0;
        camera_rad = camera_angle * do2rad;
        sin_rad = Math.sin(camera_rad);
        cos_rad = Math.cos(camera_rad);
        camera_mirror = 1.0;       //鏡
        camera_zoom_x = 1.0;
        camera_zoom_y = 1.0;

        display_position_x = 0.0;
        display_position_y = 0.0;
    }

    public void reset() {
        camera_position_x = 0.0;
        camera_position_y = 0.0;
        camera_angle = 0.0;
        camera_rad = camera_angle * do2rad;
        sin_rad = Math.sin(camera_rad);
        cos_rad = Math.cos(camera_rad);
        camera_mirror = 1.0;       //鏡
        camera_zoom_x = 1.0;
        camera_zoom_y = 1.0;

        display_position_x = 0.0;
        display_position_y = 0.0;
    }

    //public void set_camera_id(int i){camera_id=i;}
    public void setCameraPositionX(double d) {
        camera_position_x = d;
    }

    public void setCameraPositionY(double d) {
        camera_position_y = d;
    }

    public void setCameraAngle(double d) {
        camera_angle = d;
        camera_rad = camera_angle * do2rad;
        sin_rad = Math.sin(camera_rad);
        cos_rad = Math.cos(camera_rad);
    }

    public void setCameraMirror(double d) {
        camera_mirror = d;
    }       //鏡

    public void setCameraZoomX(double d) {
        camera_zoom_x = d;
    }

    public void setCameraZoomY(double d) {
        camera_zoom_y = d;
    }

    public void kakezan_camera_zoom_x(double d) {
        camera_zoom_x = d * camera_zoom_x;
    }

    public void kakezan_camera_zoom_y(double d) {
        camera_zoom_y = d * camera_zoom_y;
    }


    public void setDisplayPositionX(double d) {
        display_position_x = d;
    }

    public void setDisplayPositionY(double d) {
        display_position_y = d;
    }

    public void setCamera(Camera c0) {
        double d_camera_position_x = c0.getCameraPositionX();
        double d_camera_position_y = c0.getCameraPositionY();
        double d_camera_angle = c0.getCameraAngle();
        double d_camera_mirror = c0.getCameraMirror();
        double d_camera_zoom_x = c0.getCameraZoomX();
        double d_camera_zoom_y = c0.getCameraZoomY();

        double d_display_position_x = c0.getDisplayPositionX();
        double d_display_position_y = c0.getDisplayPositionY();

        setCameraPositionX(d_camera_position_x);
        setCameraPositionY(d_camera_position_y);
        setCameraAngle(d_camera_angle);
        setCameraMirror(d_camera_mirror);
        setCameraZoomX(d_camera_zoom_x);
        setCameraZoomY(d_camera_zoom_y);

        setDisplayPositionX(d_display_position_x);
        setDisplayPositionY(d_display_position_y);
    }

    public void display() {//Display various parameters

        System.out.println("camera.hyouji() VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV ");
        System.out.println("camera_ichi_x = " + camera_position_x);
        System.out.println("camera_ichi_y = " + camera_position_y);
        System.out.println("camera_kakudo = " + camera_angle);
        System.out.println("camera_kagami = " + camera_mirror);
        System.out.println("camera_bairitsu_x = " + camera_zoom_x);
        System.out.println("camera_bairitsu_y = " + camera_zoom_y);

        System.out.println("hyouji_ichi_x = " + display_position_x);
        System.out.println("hyouji_ichi_y = " + display_position_y);
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ");
    }


    public double getCameraPositionX() {
        return camera_position_x;
    }

    public double getCameraPositionY() {
        return camera_position_y;
    }

    public double getCameraAngle() {
        return camera_angle;
    }

    public double getCameraMirror() {
        return camera_mirror;
    }       //鏡

    public double getCameraZoomX() {
        return camera_zoom_x;
    }

    public double getCameraZoomY() {
        return camera_zoom_y;
    }

    public double getDisplayPositionX() {
        return display_position_x;
    }

    public double getDisplayPositionY() {
        return display_position_y;
    }

    public void set_camera_ichi(Point p) {
        setCameraPositionX(p.getX());
        setCameraPositionY(p.getY());
    }

    public void set_hyouji_ichi(Point p) {
        setDisplayPositionX(p.getX());
        setDisplayPositionY(p.getY());
    }


    public Point get_camera_position() {
        Point t_ichi = new Point();
        t_ichi.setX(camera_position_x);
        t_ichi.setY(camera_position_y);
        return t_ichi;
    }


    public Point object2TV(Point t_ob) {
        Point t_tv = new Point();
        double x1, y1;
        double x2, y2;
        x1 = t_ob.getX() - camera_position_x;
        y1 = t_ob.getY() - camera_position_y;
        x2 = cos_rad * x1 + sin_rad * y1;
        y2 = -sin_rad * x1 + cos_rad * y1;

        x2 = x2 * camera_mirror;       //鏡
        x2 = x2 * camera_zoom_x;
        y2 = y2 * camera_zoom_y;
        t_tv.setX(x2 + display_position_x);
        t_tv.setY(y2 + display_position_y);
        return t_tv;
    }


    public LineSegment object2TV(LineSegment s_ob) {
        LineSegment s_tv = new LineSegment();
        s_tv.set(s_ob);
        s_tv.setA(object2TV(s_ob.getA()));
        s_tv.setB(object2TV(s_ob.getB()));
        return s_tv;
    }


    public Point TV2object(Point t_tv) {
        Point t_ob = new Point();
        double x1, y1;
        double x2, y2;
        x1 = t_tv.getX();
        y1 = t_tv.getY();
        x1 = x1 - display_position_x;
        y1 = y1 - display_position_y;
        x1 = x1 / camera_zoom_x;
        y1 = y1 / camera_zoom_y;

        x1 = x1 * camera_mirror;       //鏡

        x2 = cos_rad * x1 - sin_rad * y1;
        y2 = sin_rad * x1 + cos_rad * y1;

        t_ob.setX(x2 + camera_position_x);
        t_ob.setY(y2 + camera_position_y);
        return t_ob;
    }

    public LineSegment TV2object(LineSegment s_tv) {
        LineSegment s_ob = new LineSegment();
        s_ob.set(s_tv);
        s_ob.setA(TV2object(s_tv.getA()));
        s_ob.setB(TV2object(s_tv.getB()));
        return s_ob;
    }

    public void hyouji_ichi_idou(Point tuika) {
        display_position_x = display_position_x + tuika.getX();
        display_position_y = display_position_y + tuika.getY();
    }

    //TV上の表示は変化しないようにして、TV上の座標Ten　P　に対応する、被写体の位置にcamera位置をあわせる。
    public void camera_ichi_sitei_from_TV(Point p) {
        set_camera_ichi(TV2object(p));
        set_hyouji_ichi(p);
    }


}
