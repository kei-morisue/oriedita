package origami_editor.editor.egaki_syokunin;

import origami_editor.editor.LineColor;
import origami_editor.editor.LineStyle;
import origami_editor.editor.MouseMode;
import origami_editor.editor.undo_box.Undo_Box;
import origami_editor.graphic2d.circle.Circle;
import origami_editor.graphic2d.grid.Grid;
import origami_editor.graphic2d.linesegment.LineSegment;
import origami_editor.graphic2d.oritacalc.OritaCalc;
import origami_editor.graphic2d.oritacalc.straightline.StraightLine;
import origami_editor.graphic2d.oritaoekaki.OritaDrawing;
import origami_editor.graphic2d.point.Point;
import origami_editor.graphic2d.polygon.Polygon;
import origami_editor.record.memo.Memo;
import origami_editor.tools.camera.Camera;
import origami_editor.tools.linestore.LineSegmentSet;
import origami_editor.tools.orisensyuugou.FoldLineSet;
import origami_editor.editor.App;
import origami_editor.editor.egaki_syokunin.egaki_syokunin_dougubako.Drawing_Worker_Toolbox;
import origami_editor.seiretu.narabebako.SortingBox_int_double;
import origami_editor.seiretu.narabebako.int_double;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class Drawing_Worker {
    private final LineSegmentSet sen_s = new LineSegmentSet();    //Instantiation of basic branch structure
    public FoldLineSet foldLines = new FoldLineSet();    //Store polygonal lines
    public FoldLineSet vonoroiLines = new FoldLineSet();    //Store Voronoi diagram lines
    public Grid grid = new Grid();
    public int i_drawing_stage;//Stores information about the stage of the procedure for drawing a polygonal line
    public int i_candidate_stage;//Stores information about which candidate for the procedure to draw a polygonal line
    public Polygon operationFrameBox = new Polygon(4);    //Instantiation of selection box (TV coordinates)
    public boolean i_O_F_C = false;//Input status of a line segment representing the outer circumference when checking the outer circumference. 0 is input not completed, 1 is input completed (line segment is a closed polygon)
    int pointSize = 1;
    LineColor lineColor;//Line segment color
    LineColor auxLineColor = LineColor.ORANGE_4;//Auxiliary line color
    boolean i_kou_mitudo_nyuuryoku = false;//1 if you use the input assist function for fine grid display, 0 if you do not use it
    Color circle_custom_color;//Stores custom colors for circles and auxiliary hot lines
    Undo_Box Ubox = new Undo_Box();
    Undo_Box h_Ubox = new Undo_Box();
    origami_editor.graphic2d.point.Point closest_point = new origami_editor.graphic2d.point.Point(100000.0, 100000.0); //マウス最寄の点。get_moyori_ten(Ten p)で求める。
    LineSegment closest_lineSegment = new LineSegment(100000.0, 100000.0, 100000.0, 100000.1); //マウス最寄の線分
    LineSegment closest_step_lineSegment = new LineSegment(100000.0, 100000.0, 100000.0, 100000.1); //マウス最寄のstep線分(線分追加のための準備をするための線分)。なお、ここで宣言する必要はないので、どこで宣言すべきか要検討20161113
    Circle closest_circumference = new Circle(100000.0, 100000.0, 10.0, LineColor.PURPLE_8); //Circle with the circumference closest to the mouse
    FoldLineAdditionalInputMode i_foldLine_additional = FoldLineAdditionalInputMode.POLY_LINE_0;//= 0 is polygonal line input = 1 is auxiliary line input mode (when inputting a line segment, these two). When deleting a line segment, the value becomes as follows. = 0 is the deletion of the polygonal line, = 1 is the deletion of the auxiliary picture line, = 2 is the deletion of the black line, = 3 is the deletion of the auxiliary live line, = 4 is the folding line, the auxiliary live line and the auxiliary picture line.
    FoldLineSet auxLines = new FoldLineSet();    //Store auxiliary lines
    Drawing_Worker_Toolbox e_s_dougubako = new Drawing_Worker_Toolbox(foldLines);
    int id_angle_system = 8;//180 / id_angle_system represents the angular system. For example, if id_angle_system = 3, 180/3 = 60 degrees, if id_angle_system = 5, 180/5 = 36 degrees
    double d_angle_system;//d_angle_system=180.0/(double)id_angle_system
    double angle;
    int foldLineDividingNumber = 1;
    double d_internalDivisionRatio_s;
    double d_internalDivisionRatio_t;
    double d_restricted_angle_1;
    double d_restricted_angle_2;
    double d_restricted_angle_3;
    int numPolygonCorners = 5;
    double d_decision_width = 50.0;//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<入力点が既存の点や線分と近いかどうかを判定する時の値
    int i_circle_drawing_stage;//Stores information about which stage of the circle drawing procedure
    LineSegment[] line_step = new LineSegment[1024];//Used for temporary display when drawing. Do not actually use line_step [0], but use it from line_step [1].
    Circle[] circle_step = new Circle[1024];//Used for temporary display when drawing. circle_step [0] is not actually used, but is used from circle_step [1].
    LineSegment[] line_candidate = new LineSegment[16];//Used for displaying selection candidates when drawing. line_candidate [0] is not actually used, it is used from line_candidate [1].
    Circle[] circle_candidate = new Circle[16];//Used for displaying selection candidates when drawing. circle_candidate [0] is not actually used, it is used from circle_candidate [1].
    double measured_length_1 = 0.0;
    double measured_length_2 = 0.0;
    double measured_angle_1 = 0.0;
    double measured_angle_2 = 0.0;
    double measured_angle_3 = 0.0;
    String text_cp_setumei;
    String text_cp_setumei2;
    String s_title; //Used to hold the title that appears at the top of the frame
    Camera camera = new Camera();
    boolean check1 = false;//=0 check1を実施しない、1=実施する　　
    boolean check2 = false;//=0 check2を実施しない、1=実施する　
    boolean check3 = false;//=0 check3を実施しない、1=実施する　
    boolean check4 = false;//=0 check4を実施しない、1=実施する　
    //---------------------------------
    int i_ck4_color_toukado = 100;
    App app;
    LineColor icol_temp = LineColor.BLACK_0;//Used for temporary memory of color specification
    //i_mouse_modeA==61//長方形内選択（paintの選択に似せた選択機能）の時に使う
    origami_editor.graphic2d.point.Point operationFrame_p1 = new origami_editor.graphic2d.point.Point();//TV座標
    origami_editor.graphic2d.point.Point operationFrame_p2 = new origami_editor.graphic2d.point.Point();//TV座標
    origami_editor.graphic2d.point.Point operationFrame_p3 = new origami_editor.graphic2d.point.Point();//TV座標
    origami_editor.graphic2d.point.Point operationFrame_p4 = new origami_editor.graphic2d.point.Point();//TV座標
    OperationFrameMode operationFrameMode = OperationFrameMode.NONE_0;// = 1 Create a new selection box. = 2 Move points. 3 Move the sides. 4 Move the selection box.
    origami_editor.graphic2d.point.Point p = new origami_editor.graphic2d.point.Point();
    ArrayList<LineSegment> lineSegment_vonoroi_onePoint = new ArrayList<>(); //Line segment around one point in Voronoi diagram
    // ****************************************************************************************************************************************
    // **************　Variable definition so far　****************************************************************************************************
    // ****************************************************************************************************************************************
    // ------------------------------------------------------------------------------------------------------------
    int i_mouse_modeA_62_point_overlapping;//Newly added p does not overlap with previously added Point = 0, overlaps = 1
    SortingBox_int_double entyou_kouho_nbox = new SortingBox_int_double();
    int i_dousa_mode = 0;
    int i_dousa_mode_henkou_kanousei = 0;//動作モード変更可能性。0なら不可能、1なら可能。
    origami_editor.graphic2d.point.Point moyori_point_memo = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_1 = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_2 = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_3 = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_4 = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_a = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_b = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_c = new origami_editor.graphic2d.point.Point();
    origami_editor.graphic2d.point.Point p19_d = new origami_editor.graphic2d.point.Point();
    //--------------------------------------------
    int i_select_mode = 0;//=0は通常のセレクト操作
    //30 30 30 30 30 30 30 30 30 30 30 30 除け_線_変換
    int minrid_30;
    int i_step_for_move_4p = 0;
    //39 39 39 39 39 39 39    i_mouse_modeA==39　;折り畳み可能線入力  qqqqqqqqq
    int i_step_for_copy_4p = 0;//i_step_for_copy_4p=2の場合は、step線が1本だけになっていて、次の操作で入力折線が確定する状態
    int i_takakukei_kansei = 0;//多角形が完成したら1、未完成なら0
    // ------------
    FoldLineAdditionalInputMode i_foldLine_additional_old = FoldLineAdditionalInputMode.POLY_LINE_0;
    int i_ck4_color_toukado_sabun = 10;
    public Drawing_Worker(double r0, App app0) {  //コンストラクタ
        app = app0;

        lineColor = LineColor.BLACK_0;

        for (int i = 0; i <= 1024 - 1; i++) {
            line_step[i] = new LineSegment();
        }
        for (int i = 0; i <= 1024 - 1; i++) {
            circle_step[i] = new Circle();
        }

        for (int i = 0; i <= 16 - 1; i++) {
            line_candidate[i] = new LineSegment();
        }
        for (int i = 0; i <= 16 - 1; i++) {
            circle_candidate[i] = new Circle();
        }

        text_cp_setumei = "1/";
        text_cp_setumei2 = " ";
        s_title = "no title";

        reset();
    }

    public void reset() {
        pointSize = 1;
        foldLines.reset();
        auxLines.reset();

        camera.reset();
        i_drawing_stage = 0;
        i_circle_drawing_stage = 0;
    }

    public void reset_2() {
        //Enter the paper square (start)
        foldLines.addLine(-200.0, -200.0, -200.0, 200.0, LineColor.BLACK_0);
        foldLines.addLine(-200.0, -200.0, 200.0, -200.0, LineColor.BLACK_0);
        foldLines.addLine(200.0, 200.0, -200.0, 200.0, LineColor.BLACK_0);
        foldLines.addLine(200.0, 200.0, 200.0, -200.0, LineColor.BLACK_0);
        //Enter the paper square (end)
    }

    public void measurement_display() {
        app.measured_length_1_display(measured_length_1);
        app.measured_length_2_display(measured_length_2);

        app.measured_angle_1_display(measured_angle_1);
        app.measured_angle_2_display(measured_angle_2);
        app.measured_angle_3_display(measured_angle_3);
    }

    public void Memo_jyouhou_toridasi(Memo memo1) {

        boolean i_reading;
        String[] st;
        String[] s;

        // Loading the camera settings for the development view
        i_reading = false;
        for (int i = 1; i <= memo1.getLineCount(); i++) {
            String str = memo1.getLine(i);

            if (str.equals("<camera_of_orisen_nyuuryokuzu>")) {
                i_reading = true;
            } else if (str.equals("</camera_of_orisen_nyuuryokuzu>")) {
                i_reading = false;
            } else {
                if (i_reading) {
                    st = str.split(">", 2);// <-----------------------------------２つに分割するときは2を指定
                    if (st[0].equals("<camera_ichi_x")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraPositionX(Double.parseDouble(s[0]));
                    }
                    if (st[0].equals("<camera_ichi_y")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraPositionY(Double.parseDouble(s[0]));
                    }
                    if (st[0].equals("<camera_kakudo")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraAngle(Double.parseDouble(s[0]));
                    }
                    if (st[0].equals("<camera_kagami")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraMirror(Double.parseDouble(s[0]));
                    }

                    if (st[0].equals("<camera_bairitsu_x")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraZoomX(Double.parseDouble(s[0]));
                    }
                    if (st[0].equals("<camera_bairitsu_y")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setCameraZoomY(Double.parseDouble(s[0]));
                    }

                    if (st[0].equals("<hyouji_ichi_x")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setDisplayPositionX(Double.parseDouble(s[0]));
                    }
                    if (st[0].equals("<hyouji_ichi_y")) {
                        s = st[1].split("<", 2);
                        app.camera_of_orisen_input_diagram.setDisplayPositionY(Double.parseDouble(s[0]));
                    }
                }
            }
        }

        // ----------------------------------------- チェックボックス等の設定の読み込み
        i_reading = false;
        for (int i = 1; i <= memo1.getLineCount(); i++) {
            String str = memo1.getLine(i);

            if (str.equals("<settei>")) {
                i_reading = true;
            } else if (str.equals("</settei>")) {
                i_reading = false;
            } else {
                if (i_reading) {
                    st = str.split(">", 2);// <-----------------------------------２つに分割するときは2を指定

                    if (st[0].equals("<ckbox_mouse_settei")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_mouse_settings.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_ten_sagasi")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_point_search.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_ten_hanasi")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_ten_hanasi.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_kou_mitudo_nyuuryoku")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_kou_mitudo_nyuuryoku.setSelected(selected);
                        set_i_kou_mitudo_nyuuryoku(selected);
                    }

                    if (st[0].equals("<ckbox_bun")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_bun.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_cp")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_cp.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_a0")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_a0.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_a1")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_a1.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_mejirusi")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_mark.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_cp_ue")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_cp_ue.setSelected(selected);
                    }

                    if (st[0].equals("<ckbox_oritatami_keika")) {
                        s = st[1].split("<", 2);

                        boolean selected = Boolean.parseBoolean(s[0].trim());
                        app.ckbox_oritatami_keika.setSelected(selected);
                    }

                    if (st[0].equals("<iTenkaizuSenhaba")) {
                        s = st[1].split("<", 2);
                        app.iLineWidth = Integer.parseInt(s[0]);
                    }

                    if (st[0].equals("<ir_ten")) {
                        s = st[1].split("<", 2);
                        app.pointSize = Integer.parseInt(s[0]);
                        setPointSize(app.pointSize);
                    }

                    if (st[0].equals("<i_orisen_hyougen")) {
                        s = st[1].split("<", 2);
                        app.lineStyle = LineStyle.from(s[0].trim());
                    }

                    if (st[0].equals("<i_anti_alias")) {
                        s = st[1].split("<", 2);
                        app.antiAlias = Boolean.parseBoolean(s[0].trim());
                    }
                }
            }
        }

        // ----------------------------------------- 格子設定の読み込み

        i_reading = false;
        for (int i = 1; i <= memo1.getLineCount(); i++) {
            String str = memo1.getLine(i);

            if (str.equals("<Kousi>")) {
                i_reading = true;
            } else if (str.equals("</Kousi>")) {
                i_reading = false;
            } else {
                if (i_reading) {
                    st = str.split(">", 2);// <-----------------------------------２つに分割するときは2を指定

                    if (st[0].equals("<i_kitei_jyoutai")) {
                        s = st[1].split("<", 2);
                        setBaseState(Grid.State.from(s[0]));
                    }

                    if (st[0].equals("<nyuuryoku_kitei")) {
                        s = st[1].split("<", 2);
                        app.text1.setText(s[0]);
                        app.set_grid_bunkatu_suu();

                    }

                    if (st[0].equals("<memori_kankaku")) {
                        s = st[1].split("<", 2);
                        app.scale_interval = Integer.parseInt(s[0]);
                        app.text25.setText(s[0]);

                        set_a_to_parallel_scale_interval(app.scale_interval);
                        set_b_to_parallel_scale_interval(app.scale_interval);
                    }

                    if (st[0].equals("<a_to_heikouna_memori_iti")) {
                        s = st[1].split("<", 2);
                        grid.set_a_to_parallel_scale_position(Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<b_to_heikouna_memori_iti")) {
                        s = st[1].split("<", 2);
                        grid.set_b_to_parallel_scale_position(Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<kousi_senhaba")) {
                        s = st[1].split("<", 2);
                        grid.setGridLineWidth(Integer.parseInt(s[0]));
                    }

                    if (st[0].equals("<d_kousi_x_a")) {
                        s = st[1].split("<", 2);
                        app.text18.setText(s[0]);
                    }
                    if (st[0].equals("<d_kousi_x_b")) {
                        s = st[1].split("<", 2);
                        app.text19.setText(s[0]);
                    }
                    if (st[0].equals("<d_kousi_x_c")) {
                        s = st[1].split("<", 2);
                        app.text20.setText(s[0]);
                    }

                    if (st[0].equals("<d_kousi_y_a")) {
                        s = st[1].split("<", 2);
                        app.text21.setText(s[0]);
                    }
                    if (st[0].equals("<d_kousi_y_b")) {
                        s = st[1].split("<", 2);
                        app.text22.setText(s[0]);
                    }
                    if (st[0].equals("<d_kousi_y_c")) {
                        s = st[1].split("<", 2);
                        app.text23.setText(s[0]);
                    }

                    if (st[0].equals("<d_kousi_kakudo")) {
                        s = st[1].split("<", 2);
                        app.text24.setText(s[0]);
                    }

                    app.setGrid();
                }
            }
        }

        // ----------------------------------------- 格子色設定の読み込み
        int i_grid_color_R = 0;
        int i_grid_color_G = 0;
        int i_grid_color_B = 0;
        int i_grid_memori_color_R = 0;
        int i_grid_memori_color_G = 0;
        int i_grid_memori_color_B = 0;

        boolean i_Grid_iro_yomikomi = false;//Kousi_iroの読み込みがあったら1、なければ0
        i_reading = false;
        for (int i = 1; i <= memo1.getLineCount(); i++) {
            String str = memo1.getLine(i);

            if (str.equals("<Kousi_iro>")) {
                i_reading = true;
                i_Grid_iro_yomikomi = true;
            } else if (str.equals("</Kousi_iro>")) {
                i_reading = false;
            } else {
                if (i_reading) {
                    st = str.split(">", 2);// <-----------------------------------２つに分割するときは2を指定

                    if (st[0].equals("<kousi_color_R")) {
                        s = st[1].split("<", 2);
                        i_grid_color_R = (Integer.parseInt(s[0]));
                    }        //  System.out.println(Integer.parseInt(s[0])) ;
                    if (st[0].equals("<kousi_color_G")) {
                        s = st[1].split("<", 2);
                        i_grid_color_G = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<kousi_color_B")) {
                        s = st[1].split("<", 2);
                        i_grid_color_B = (Integer.parseInt(s[0]));
                    }

                    if (st[0].equals("<kousi_memori_color_R")) {
                        s = st[1].split("<", 2);
                        i_grid_memori_color_R = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<kousi_memori_color_G")) {
                        s = st[1].split("<", 2);
                        i_grid_memori_color_G = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<kousi_memori_color_B")) {
                        s = st[1].split("<", 2);
                        i_grid_memori_color_B = (Integer.parseInt(s[0]));
                    }
                }
            }
        }

        if (i_Grid_iro_yomikomi) {//Grid_iroの読み込みがあったら1、なければ0
            grid.setGridColor(new Color(i_grid_color_R, i_grid_color_G, i_grid_color_B)); //gridの色

            System.out.println("i_kousi_memori_color_R= " + i_grid_memori_color_R);
            System.out.println("i_kousi_memori_color_G= " + i_grid_memori_color_G);
            System.out.println("i_kousi_memori_color_B= " + i_grid_memori_color_B);
            app.kus.setGridScaleColor(new Color(i_grid_memori_color_R, i_grid_memori_color_G, i_grid_memori_color_B)); //grid_memoriの色

        }


        // 折り上がり図設定の読み込み -------------------------------------------------------------------------

        int i_oriagarizu_F_color_R = 0;
        int i_oriagarizu_F_color_G = 0;
        int i_oriagarizu_F_color_B = 0;

        int i_oriagarizu_B_color_R = 0;
        int i_oriagarizu_B_color_G = 0;
        int i_oriagarizu_B_color_B = 0;

        int i_oriagarizu_L_color_R = 0;
        int i_oriagarizu_L_color_G = 0;
        int i_oriagarizu_L_color_B = 0;


        boolean i_oriagarizu_yomikomi = false;//oriagarizuの読み込みがあったら1、なければ0
        i_reading = false;
        for (int i = 1; i <= memo1.getLineCount(); i++) {
            String str = memo1.getLine(i);

            if (str.equals("<oriagarizu>")) {
                i_reading = true;
                i_oriagarizu_yomikomi = true;
            } else if (str.equals("</oriagarizu>")) {
                i_reading = false;
            } else {
                if (i_reading) {
                    st = str.split(">", 2);// <-----------------------------------２つに分割するときは2を指定

                    if (st[0].equals("<oriagarizu_F_color_R")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_F_color_R = (Integer.parseInt(s[0]));
                    }        //  System.out.println(Integer.parseInt(s[0])) ;
                    if (st[0].equals("<oriagarizu_F_color_G")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_F_color_G = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<oriagarizu_F_color_B")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_F_color_B = (Integer.parseInt(s[0]));
                    }

                    if (st[0].equals("<oriagarizu_B_color_R")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_B_color_R = (Integer.parseInt(s[0]));
                    }        //  System.out.println(Integer.parseInt(s[0])) ;
                    if (st[0].equals("<oriagarizu_B_color_G")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_B_color_G = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<oriagarizu_B_color_B")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_B_color_B = (Integer.parseInt(s[0]));
                    }

                    if (st[0].equals("<oriagarizu_L_color_R")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_L_color_R = (Integer.parseInt(s[0]));
                    }        //  System.out.println(Integer.parseInt(s[0])) ;
                    if (st[0].equals("<oriagarizu_L_color_G")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_L_color_G = (Integer.parseInt(s[0]));
                    }
                    if (st[0].equals("<oriagarizu_L_color_B")) {
                        s = st[1].split("<", 2);
                        i_oriagarizu_L_color_B = (Integer.parseInt(s[0]));
                    }
                }
            }
        }

        if (i_oriagarizu_yomikomi) {
            app.OZ.ct_worker.set_F_color(new Color(i_oriagarizu_F_color_R, i_oriagarizu_F_color_G, i_oriagarizu_F_color_B)); //表面の色
            app.Button_F_color.setBackground(new Color(i_oriagarizu_F_color_R, i_oriagarizu_F_color_G, i_oriagarizu_F_color_B));    //ボタンの色設定

            app.OZ.ct_worker.set_B_color(new Color(i_oriagarizu_B_color_R, i_oriagarizu_B_color_G, i_oriagarizu_B_color_B));//裏面の色
            app.Button_B_color.setBackground(new Color(i_oriagarizu_B_color_R, i_oriagarizu_B_color_G, i_oriagarizu_B_color_B));//ボタンの色設定

            app.OZ.ct_worker.set_L_color(new Color(i_oriagarizu_L_color_R, i_oriagarizu_L_color_G, i_oriagarizu_L_color_B));        //線の色
            app.Button_L_color.setBackground(new Color(i_oriagarizu_L_color_R, i_oriagarizu_L_color_G, i_oriagarizu_L_color_B));        //ボタンの色設定
        }
    }

    public String setMemo_for_redo_undo(Memo memo1) {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<undo,redoでのkiroku復元用
        return foldLines.setMemo(memo1);
    }

    public void setMemo_for_reading(Memo memo1) {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<For reading data
        Memo_jyouhou_toridasi(memo1);
        foldLines.setMemo(memo1);
        auxLines.h_setMemo(memo1);
    }

    public void setMemo_for_reading_tuika(Memo memo1) {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<For reading data
        double addx, addy;

        FoldLineSet ori_s_temp = new FoldLineSet();    //追加された折線だけ取り出すために使う
        ori_s_temp.setMemo(memo1);//追加された折線だけ取り出してori_s_tempを作る
        addx = foldLines.get_x_max() + 100.0 - ori_s_temp.get_x_min();
        addy = foldLines.get_y_max() - ori_s_temp.get_y_max();

        ori_s_temp.move(addx, addy);//全体を移動する

        int sousuu_old = foldLines.getTotal();
        foldLines.addMemo(ori_s_temp.getMemo());
        int sousuu_new = foldLines.getTotal();
        foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, sousuu_new);

        foldLines.unselect_all();
        record();
    }

    public void h_setMemo(Memo memo1) {
        auxLines.h_setMemo(memo1);
    }

    public void setCamera(Camera cam0) {
        camera.setCamera(cam0);

        calc_d_decision_haba();
    }

    public void set_sen_tokutyuu_color(Color c0) {
        circle_custom_color = c0;
    }

    public void allMountainValleyChange() {
        foldLines.allMountainValleyChange();
    }

    public void branch_trim(double r) {
        foldLines.branch_trim(r);
    }

    public LineSegmentSet get() {
        sen_s.setMemo(foldLines.getMemo());
        return sen_s;
    }

    public LineSegmentSet get_for_folding() {
        sen_s.setMemo(foldLines.getMemo_for_folding());
        return sen_s;
    }

    //折畳み推定用にselectされた線分集合の折線数を intとして出力する。//icolが3(cyan＝水色)以上の補助線はカウントしない
    public int getFoldLineTotalForSelectFolding() {
        return foldLines.getFoldLineTotalForSelectFolding();
    }

    public LineSegmentSet getForSelectFolding() {//selectした折線で折り畳み推定をする。
        sen_s.setMemo(foldLines.getMemo_for_select_folding());
        return sen_s;
    }

    //--------------------------------------------
    //public void set_r(double r0){r_ten=r0;}
    public void setPointSize(int i0) {
        pointSize = i0;
    }

    public void set_grid_bunkatu_suu(int i) {
        grid.set_grid_bunkatu_suu(i);
        text_cp_setumei = "1/" + grid.divisionNumber();
        calc_d_decision_haba();
    }

    public void calc_d_decision_haba() {
        d_decision_width = grid.d_width() / 4.0;
        if (camera.getCameraZoomX() * d_decision_width < 10.0) {
            d_decision_width = 10.0 / camera.getCameraZoomX();
        }
    }

    public void set_d_grid(double dkxn, double dkyn, double dkk) {
        grid.set_d_grid(dkxn, dkyn, dkk);
    }

    public int getTotal() {
        return foldLines.getTotal();
    }

    public Memo getMemo() {
        return foldLines.getMemo();
    }

    public Memo getMemo(String s_title) {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<undo,redoのkiroku用
        Memo memo_temp = new Memo();
        memo_temp.set(foldLines.getMemo(s_title));

        Memo_jyouhou_tuika(memo_temp);
        return memo_temp;
    }

    public Memo h_getMemo() {
        return auxLines.h_getMemo();
    }

    public Memo getMemo_for_export() {//<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<データ書き出し

        Memo memo_temp = new Memo();
        memo_temp.set(foldLines.getMemo());
        memo_temp.addMemo(auxLines.h_getMemo());
        Memo_jyouhou_tuika(memo_temp);
        return memo_temp;
    }

    //------------------------svgデータ書き出し
    public Memo getMemo_for_svg_export_with_camera(boolean i_bun_display, boolean i_cp_display, boolean i_a0_hyouji, boolean i_a1_hyouji, float fTenkaizuSenhaba, LineStyle lineStyle, float f_h_TenkaizuSenhaba, int p0x_max, int p0y_max, boolean i_mark_display) {//引数はカメラ設定、線幅、画面X幅、画面y高さ
        Memo memo_temp = new Memo();

        LineSegment s_tv = new LineSegment();
        origami_editor.graphic2d.point.Point a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point b = new origami_editor.graphic2d.point.Point();

        String str_stroke;
        String str_strokewidth;
        str_strokewidth = Integer.toString(app.iLineWidth);

        //Drawing of development drawing Polygonal lines other than auxiliary live lines
        if (i_cp_display) {
            for (int i = 1; i <= foldLines.getTotal(); i++) {
                LineColor color = foldLines.getColor(i);
                if (color.isFoldingLine()) {
                    switch (color) {
                        case BLACK_0:
                            str_stroke = "black";
                            break;
                        case RED_1:
                            str_stroke = "red";
                            break;
                        case BLUE_2:
                            str_stroke = "blue";
                            break;
                        default:
                            throw new IllegalStateException("Not a folding line: " + color);
                    }

                    if (lineStyle == LineStyle.BLACK_TWO_DOT || lineStyle == LineStyle.BLACK_ONE_DOT) {
                        str_stroke = "black";
                    }

                    String str_stroke_dasharray;
                    switch (lineStyle) {
                        case COLOR:
                            str_stroke_dasharray = "";
                            break;
                        case COLOR_AND_SHAPE:
                        case BLACK_ONE_DOT:
                            //基本指定A　　線の太さや線の末端の形状
                            //dash_M1,一点鎖線
                            switch (color) {
                                case RED_1:
                                    str_stroke_dasharray = "stroke-dasharray=\"10 3 3 3\"";
                                    break;
                                case BLUE_2:
                                    str_stroke_dasharray = "stroke-dasharray=\"8 8\"";
                                    break;
                                default:
                                    str_stroke_dasharray = "";
                                    break;
                            }
                            break;
                        case BLACK_TWO_DOT:
                            //基本指定A　　線の太さや線の末端の形状
                            //dash_M2,二点鎖線
                            switch (color) {
                                case RED_1:
                                    str_stroke_dasharray = "stroke-dasharray=\"10 3 3 3 3 3\"";
                                    break;
                                case BLUE_2:
                                    str_stroke_dasharray = "stroke-dasharray=\"8 8\"";
                                    break;
                                default:
                                    str_stroke_dasharray = "";
                                    break;
                            }
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }

                    s_tv.set(camera.object2TV(foldLines.get(i)));
                    a.set(s_tv.getA());
                    b.set(s_tv.getB());//a.set(s_tv.getax()+0.000001,s_tv.getay()+0.000001); b.set(s_tv.getbx()+0.000001,s_tv.getby()+0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                    BigDecimal b_ax = new BigDecimal(String.valueOf(a.getX()));
                    double x1 = b_ax.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    BigDecimal b_ay = new BigDecimal(String.valueOf(a.getY()));
                    double y1 = b_ay.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    BigDecimal b_bx = new BigDecimal(String.valueOf(b.getX()));
                    double x2 = b_bx.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    BigDecimal b_by = new BigDecimal(String.valueOf(b.getY()));
                    double y2 = b_by.setScale(2, RoundingMode.HALF_UP).doubleValue();


                    memo_temp.addLine("<line x1=\"" + x1 + "\"" +
                            " y1=\"" + y1 + "\"" +
                            " x2=\"" + x2 + "\"" +
                            " y2=\"" + y2 + "\"" +
                            " " + str_stroke_dasharray + " " +
                            " stroke=\"" + str_stroke + "\"" +
                            " stroke-width=\"" + str_strokewidth + "\"" + " />");

                    if (pointSize != 0) {
                        if (fTenkaizuSenhaba < 2.0f) {//Draw a black square at the vertex
                            int i_haba = pointSize;

                            memo_temp.addLine("<rect style=\"fill:#000000;stroke:#000000;stroke-width:1\"" +
                                    " width=\"" + (2.0 * (double) i_haba + 1.0) + "\"" +
                                    " height=\"" + (2.0 * (double) i_haba + 1.0) + "\"" +
                                    " x=\"" + (x1 - (double) i_haba) + "\"" +
                                    " y=\"" + (y1 - (double) i_haba) + "\"" +
                                    " />");

                            memo_temp.addLine("<rect style=\"fill:#000000;stroke:#000000;stroke-width:1\"" +
                                    " width=\"" + (2.0 * (double) i_haba + 1.0) + "\"" +
                                    " height=\"" + (2.0 * (double) i_haba + 1.0) + "\"" +
                                    " x=\"" + (x2 - (double) i_haba) + "\"" +
                                    " y=\"" + (y2 - (double) i_haba) + "\"" +
                                    " />");
                        }
                    }

                    if (fTenkaizuSenhaba >= 2.0f) {//  Thick line
                        if (pointSize != 0) {
                            double d_haba = (double) fTenkaizuSenhaba / 2.0 + (double) pointSize;//int i_haba=2;

                            memo_temp.addLine("<circle style=\"fill:#ffffff;stroke:#000000;stroke-width:1\"" +
                                    " r=\"" + d_haba + "\"" +
                                    " cx=\"" + x1 + "\"" +
                                    " cy=\"" + y1 + "\"" +
                                    " />");


                            memo_temp.addLine("<circle style=\"fill:#ffffff;stroke:#000000;stroke-width:1\"" +
                                    " r=\"" + d_haba + "\"" +
                                    " cx=\"" + x2 + "\"" +
                                    " cy=\"" + y2 + "\"" +
                                    " />");
                        }
                    }


                }
            }
        }


        return memo_temp;
    }

    public void Memo_jyouhou_tuika(Memo memo1) {
        memo1.addLine("<camera_of_orisen_nyuuryokuzu>");
        memo1.addLine("<camera_ichi_x>" + camera.getCameraPositionX() + "</camera_ichi_x>");
        memo1.addLine("<camera_ichi_y>" + camera.getCameraPositionY() + "</camera_ichi_y>");
        memo1.addLine("<camera_kakudo>" + camera.getCameraAngle() + "</camera_kakudo>");
        memo1.addLine("<camera_kagami>" + camera.getCameraMirror() + "</camera_kagami>");
        memo1.addLine("<camera_bairitsu_x>" + camera.getCameraZoomX() + "</camera_bairitsu_x>");
        memo1.addLine("<camera_bairitsu_y>" + camera.getCameraZoomY() + "</camera_bairitsu_y>");
        memo1.addLine("<hyouji_ichi_x>" + camera.getDisplayPositionX() + "</hyouji_ichi_x>");
        memo1.addLine("<hyouji_ichi_y>" + camera.getDisplayPositionY() + "</hyouji_ichi_y>");
        memo1.addLine("</camera_of_orisen_nyuuryokuzu>");


        memo1.addLine("<settei>");
        memo1.addLine("<ckbox_mouse_settei>" + app.ckbox_mouse_settings.isSelected() + "</ckbox_mouse_settei>");
        memo1.addLine("<ckbox_ten_sagasi>" + app.ckbox_point_search.isSelected() + "</ckbox_ten_sagasi>");
        memo1.addLine("<ckbox_ten_hanasi>" + app.ckbox_ten_hanasi.isSelected() + "</ckbox_ten_hanasi>");
        memo1.addLine("<ckbox_kou_mitudo_nyuuryoku>" + app.ckbox_kou_mitudo_nyuuryoku.isSelected() + "</ckbox_kou_mitudo_nyuuryoku>");
        memo1.addLine("<ckbox_bun>" + app.ckbox_bun.isSelected() + "</ckbox_bun>");
        memo1.addLine("<ckbox_cp>" + app.ckbox_cp.isSelected() + "</ckbox_cp>");
        memo1.addLine("<ckbox_a0>" + app.ckbox_a0.isSelected() + "</ckbox_a0>");
        memo1.addLine("<ckbox_a1>" + app.ckbox_a1.isSelected() + "</ckbox_a1>");
        memo1.addLine("<ckbox_mejirusi>" + app.ckbox_mark.isSelected() + "</ckbox_mejirusi>");
        memo1.addLine("<ckbox_cp_ue>" + app.ckbox_cp_ue.isSelected() + "</ckbox_cp_ue>");
        memo1.addLine("<ckbox_oritatami_keika>" + app.ckbox_oritatami_keika.isSelected() + "</ckbox_oritatami_keika>");
        //The thickness of the line in the development view.
        memo1.addLine("<iTenkaizuSenhaba>" + app.iLineWidth + "</iTenkaizuSenhaba>");
        //Width of vertex sign
        memo1.addLine("<ir_ten>" + app.pointSize + "</ir_ten>");
        //Express the polygonal line expression with color
        memo1.addLine("<i_orisen_hyougen>" + app.lineStyle + "</i_orisen_hyougen>");
        memo1.addLine("<i_anti_alias>" + app.antiAlias + "</i_anti_alias>");
        memo1.addLine("</settei>");

        memo1.addLine("<Kousi>");
        memo1.addLine("<i_kitei_jyoutai>" + getBaseState() + "</i_kitei_jyoutai>");
        memo1.addLine("<nyuuryoku_kitei>" + app.nyuuryoku_kitei + "</nyuuryoku_kitei>");

        memo1.addLine("<memori_kankaku>" + app.scale_interval + "</memori_kankaku>");
        memo1.addLine("<a_to_heikouna_memori_iti>" + grid.get_a_to_parallel_scale_position() + "</a_to_heikouna_memori_iti>");
        memo1.addLine("<b_to_heikouna_memori_iti>" + grid.get_b_to_parallel_scale_position() + "</b_to_heikouna_memori_iti>");
        memo1.addLine("<kousi_senhaba>" + grid.getGridLIneWidth() + "</kousi_senhaba>");

        memo1.addLine("<d_kousi_x_a>" + app.d_grid_x_a + "</d_kousi_x_a>");
        memo1.addLine("<d_kousi_x_b>" + app.d_grid_x_b + "</d_kousi_x_b>");
        memo1.addLine("<d_kousi_x_c>" + app.d_grid_x_c + "</d_kousi_x_c>");
        memo1.addLine("<d_kousi_y_a>" + app.d_grid_y_a + "</d_kousi_y_a>");
        memo1.addLine("<d_kousi_y_b>" + app.d_grid_y_b + "</d_kousi_y_b>");
        memo1.addLine("<d_kousi_y_c>" + app.d_grid_y_c + "</d_kousi_y_c>");
        memo1.addLine("<d_kousi_kakudo>" + app.d_grid_angle + "</d_kousi_kakudo>");
        memo1.addLine("</Kousi>");

        memo1.addLine("<Kousi_iro>");
        memo1.addLine("<kousi_color_R>" + grid.getGridColor().getRed() + "</kousi_color_R>");
        memo1.addLine("<kousi_color_G>" + grid.getGridColor().getGreen() + "</kousi_color_G>");
        memo1.addLine("<kousi_color_B>" + grid.getGridColor().getBlue() + "</kousi_color_B>");

        memo1.addLine("<kousi_memori_color_R>" + grid.getGridScaleColor().getRed() + "</kousi_memori_color_R>");
        memo1.addLine("<kousi_memori_color_G>" + grid.getGridScaleColor().getGreen() + "</kousi_memori_color_G>");
        memo1.addLine("<kousi_memori_color_B>" + grid.getGridScaleColor().getBlue() + "</kousi_memori_color_B>");
        memo1.addLine("</Kousi_iro>");

        memo1.addLine("<oriagarizu>");

        memo1.addLine("<oriagarizu_F_color_R>" + app.OZ.foldedFigure_F_color.getRed() + "</oriagarizu_F_color_R>");
        memo1.addLine("<oriagarizu_F_color_G>" + app.OZ.foldedFigure_F_color.getGreen() + "</oriagarizu_F_color_G>");
        memo1.addLine("<oriagarizu_F_color_B>" + app.OZ.foldedFigure_F_color.getBlue() + "</oriagarizu_F_color_B>");

        memo1.addLine("<oriagarizu_B_color_R>" + app.OZ.foldedFigure_B_color.getRed() + "</oriagarizu_B_color_R>");
        memo1.addLine("<oriagarizu_B_color_G>" + app.OZ.foldedFigure_B_color.getGreen() + "</oriagarizu_B_color_G>");
        memo1.addLine("<oriagarizu_B_color_B>" + app.OZ.foldedFigure_B_color.getBlue() + "</oriagarizu_B_color_B>");

        memo1.addLine("<oriagarizu_L_color_R>" + app.OZ.foldedFigure_L_color.getRed() + "</oriagarizu_L_color_R>");
        memo1.addLine("<oriagarizu_L_color_G>" + app.OZ.foldedFigure_L_color.getGreen() + "</oriagarizu_L_color_G>");
        memo1.addLine("<oriagarizu_L_color_B>" + app.OZ.foldedFigure_L_color.getBlue() + "</oriagarizu_L_color_B>");

        memo1.addLine("</oriagarizu>");
    }

    public void setColor(LineColor i) {
        lineColor = i;
    }

    public void point_removal() {
        foldLines.point_removal();
    }

    public void overlapping_line_removal() {
        foldLines.overlapping_line_removal();
    }

    public String undo() {
        s_title = setMemo_for_redo_undo(Ubox.undo());

        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

        return s_title;
    }

    public String redo() {
        s_title = setMemo_for_redo_undo(Ubox.redo());

        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

        return s_title;
    }

    public void setTitle(String s_title0) {
        s_title = s_title0;
    }

    public void record() {
        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

        Ubox.record(getMemo(s_title));
    }

    public void h_undo() {
        h_setMemo(h_Ubox.undo());
    }

    public void h_redo() {
        h_setMemo(h_Ubox.redo());
    }

    public void h_record() {
        h_Ubox.record(h_getMemo());
    }

    //--------------------------------------------------------------------------------------
    //Mouse operation----------------------------------------------------------------------------
    //--------------------------------------------------------------------------------------

    //------------------------------------------------------------------------------
    //Drawing the basic branch
    //------------------------------------------------------------------------------
    public void draw_with_camera(Graphics g, boolean i_bun_display, boolean i_cp_display, boolean i_a0_display, boolean i_a1_display, float fWireFrameLineWidth, LineStyle lineStyle, float f_h_WireframeLineWidth, int p0x_max, int p0y_max, boolean i_mejirusi_display) {//引数はカメラ設定、線幅、画面X幅、画面y高さ
        Graphics2D g2 = (Graphics2D) g;

        LineSegment s_tv = new LineSegment();
        origami_editor.graphic2d.point.Point a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point b = new origami_editor.graphic2d.point.Point();

        // ------------------------------------------------------

        //Drawing grid lines
        grid.draw(g, camera, p0x_max, p0y_max, i_kou_mitudo_nyuuryoku);

        BasicStroke BStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        g2.setStroke(BStroke);//Line thickness and shape of the end of the line

        //Drawing auxiliary strokes (non-interfering with polygonal lines)
        if (i_a1_display) {
            g2.setStroke(new BasicStroke(f_h_WireframeLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//Line thickness and shape of the end of the line
            for (int i = 1; i <= auxLines.getTotal(); i++) {
                g_setColor(g, auxLines.getColor(i));

                s_tv.set(camera.object2TV(auxLines.get(i)));
                a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
                b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線

                if (fWireFrameLineWidth < 2.0f) {//Draw a square at the vertex
                    g.setColor(Color.black);
                    int i_haba = pointSize;
                    g.fillRect((int) a.getX() - i_haba, (int) a.getY() - i_haba, 2 * i_haba + 1, 2 * i_haba + 1); //正方形を描く//g.fillRect(10, 10, 100, 50);長方形を描く
                    g.fillRect((int) b.getX() - i_haba, (int) b.getY() - i_haba, 2 * i_haba + 1, 2 * i_haba + 1); //正方形を描く
                }

                if (fWireFrameLineWidth >= 2.0f) {//  Thick line
                    g2.setStroke(new BasicStroke(1.0f + f_h_WireframeLineWidth % 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状

                    if (pointSize != 0) {
                        double d_haba = (double) fWireFrameLineWidth / 2.0 + (double) pointSize;

                        g.setColor(Color.white);
                        g2.fill(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                        g.setColor(Color.black);
                        g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                        g.setColor(Color.white);
                        g2.fill(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                        g.setColor(Color.black);
                        g2.draw(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
                    }

                    g2.setStroke(new BasicStroke(f_h_WireframeLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状

                }
            }
        }

        //check結果の表示

        g2.setStroke(new BasicStroke(15.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状、ここでは折線の端点の線の形状の指定

        //Check1Senbには0番目からsize()-1番目までデータが入っている
        if (check1) {
            for (int i = 0; i < foldLines.check1_size(); i++) {
                LineSegment s_temp = new LineSegment();
                s_temp.set(foldLines.check1_getLineSegment(i));
                OritaDrawing.pointingAt1(g, camera.object2TV(s_temp), 7.0, 3.0, 1);
            }
        }

        if (check2) {
            for (int i = 0; i < foldLines.check2_size(); i++) {
                LineSegment s_temp = new LineSegment();
                s_temp.set(foldLines.check2_getLineSegment(i));
                OritaDrawing.pointingAt2(g, camera.object2TV(s_temp), 7.0, 3.0, 1);
            }
        }

        g2.setStroke(new BasicStroke(25.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状、ここでは折線の端点の線の形状の指定


        //Check4Senbには0番目からsize()-1番目までデータが入っている
        //System.out.println("foldLines.check4_size() = "+foldLines.check4_size());
        if (check4) {
            for (int i = 0; i < foldLines.check4_size(); i++) {
                LineSegment s_temp = new LineSegment();
                s_temp.set(foldLines.check4_getLineSegment(i));
                OritaDrawing.pointingAt4(g, camera.object2TV(s_temp), i_ck4_color_toukado);
            }
        }


        //Check3Senbには0番目からsize()-1番目までデータが入っている
        if (check3) {
            for (int i = 0; i < foldLines.check3_size(); i++) {
                LineSegment s_temp = new LineSegment();
                s_temp.set(foldLines.check3_getLineSegment(i));
                //OO.jyuuji(g,camera.object2TV(s_temp.geta()), 7.0 , 3.0 , 1);
                OritaDrawing.pointingAt3(g, camera.object2TV(s_temp), 7.0, 3.0, 1);
            }
        }


        //System.out.println(" E 20170201_4");

        //camera中心を十字で描く
        if (i_mejirusi_display) {
            OritaDrawing.cross(g, camera.object2TV(camera.getCameraPosition()), 5.0, 2.0, LineColor.CYAN_3);
        }

        //円を描く　
        if (i_a0_display) {
            for (int i = 1; i <= foldLines.numCircles(); i++) {

                double d_haba;
                Circle e_temp = new Circle();
                e_temp.set(foldLines.getCircle(i));

                a.set(camera.object2TV(e_temp.getCenter()));//この場合のaは描画座標系での円の中心の位置
                //a.set(a.getx()+0.000001,a.gety()+0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状
                //g.setColor(Color.cyan);


                if (e_temp.getCustomized() == 0) {
                    g_setColor(g, e_temp.getColor());
                } else if (e_temp.getCustomized() == 1) {
                    g.setColor(e_temp.getCustomizedColor());
                }


                //円周の描画
                d_haba = e_temp.getRadius() * camera.getCameraZoomX();//d_habaは描画時の円の半径。なお、camera.get_camera_bairitsu_x()＝camera.get_camera_bairitsu_y()を前提としている。
                g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
            }
        }


        //円の中心の描画
        if (i_a0_display) {
            for (int i = 1; i <= foldLines.numCircles(); i++) {
                double d_haba;
                Circle e_temp = new Circle();
                e_temp.set(foldLines.getCircle(i));

                a.set(camera.object2TV(e_temp.getCenter()));//この場合のaは描画座標系での円の中心の位置
                //a.set(a.getx()+0.000001,a.gety()+0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状
                g.setColor(new Color(0, 255, 255, 255));

                //円の中心の描画
                if (fWireFrameLineWidth < 2.0f) {//中心の黒い正方形を描く
                    g.setColor(Color.black);
                    int i_haba = pointSize;
                    g.fillRect((int) a.getX() - i_haba, (int) a.getY() - i_haba, 2 * i_haba + 1, 2 * i_haba + 1); //正方形を描く//g.fillRect(10, 10, 100, 50);長方形を描く
                }

                if (fWireFrameLineWidth >= 2.0f) {//  太線指定時の中心を示す黒い小円を描く
                    g2.setStroke(new BasicStroke(1.0f + fWireFrameLineWidth % 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状、ここでは折線の端点の線の形状の指定
                    if (pointSize != 0) {
                        d_haba = (double) fWireFrameLineWidth / 2.0 + (double) pointSize;


                        g.setColor(Color.white);
                        g2.fill(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                        g.setColor(Color.black);
                        g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
                    }
                }
            }
        }

        //selectの描画
        g2.setStroke(new BasicStroke(fWireFrameLineWidth * 2.0f + 2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状
        for (int i = 1; i <= foldLines.getTotal(); i++) {
            if (foldLines.get_select(i) == 2) {
                g.setColor(Color.green);

                s_tv.set(camera.object2TV(foldLines.get(i)));

                a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
                b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線
            }
        }

        //展開図の描画 補助活線のみ
        if (i_a0_display) {
            for (int i = 1; i <= foldLines.getTotal(); i++) {
                if (foldLines.getColor(i) == LineColor.CYAN_3) {

                    g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状

                    if (foldLines.getLineCustomized(i) == 0) {
                        g_setColor(g, foldLines.getColor(i));
                    } else if (foldLines.getLineCustomized(i) == 1) {
                        g.setColor(foldLines.getLineCustomizedColor(i));
                    }

                    s_tv.set(camera.object2TV(foldLines.get(i)));
                    a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
                    b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                    g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線

                    if (fWireFrameLineWidth < 2.0f) {//頂点の黒い正方形を描く
                        g.setColor(Color.black);
                        int i_haba = pointSize;
                        g.fillRect((int) a.getX() - i_haba, (int) a.getY() - i_haba, 2 * i_haba + 1, 2 * i_haba + 1); //正方形を描く//g.fillRect(10, 10, 100, 50);長方形を描く
                        g.fillRect((int) b.getX() - i_haba, (int) b.getY() - i_haba, 2 * i_haba + 1, 2 * i_haba + 1); //正方形を描く
                    }

                    if (fWireFrameLineWidth >= 2.0f) {//  太線
                        g2.setStroke(new BasicStroke(1.0f + fWireFrameLineWidth % 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状、ここでは折線の端点の線の形状の指定
                        if (pointSize != 0) {
                            double d_haba = (double) fWireFrameLineWidth / 2.0 + (double) pointSize;

                            g.setColor(Color.white);
                            g2.fill(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));


                            g.setColor(Color.black);
                            g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                            g.setColor(Color.white);
                            g2.fill(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                            g.setColor(Color.black);
                            g2.draw(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
                        }
                    }
                }
            }

        }

        //展開図の描画  補助活線以外の折線
        if (i_cp_display) {

            g.setColor(Color.black);

            float[] dash_M1 = {10.0f, 3.0f, 3.0f, 3.0f};//一点鎖線
            float[] dash_M2 = {10.0f, 3.0f, 3.0f, 3.0f, 3.0f, 3.0f};//二点鎖線
            float[] dash_V = {8.0f, 8.0f};//破線

            g.setColor(Color.black);
            for (int i = 1; i <= foldLines.getTotal(); i++) {
                if (foldLines.getColor(i) != LineColor.CYAN_3) {
                    switch (lineStyle) {
                        case COLOR:
                            g_setColor(g, foldLines.getColor(i));
                            g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状
                            break;
                        case COLOR_AND_SHAPE:
                            g_setColor(g, foldLines.getColor(i));
                            if (foldLines.getColor(i) == LineColor.BLACK_0) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                            }//基本指定A　　線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.RED_1) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_M1, 0.0f));
                            }//一点鎖線//線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.BLUE_2) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_V, 0.0f));
                            }//破線//線の太さや線の末端の形状
                            break;
                        case BLACK_ONE_DOT:
                            if (foldLines.getColor(i) == LineColor.BLACK_0) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                            }//基本指定A　　線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.RED_1) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_M1, 0.0f));
                            }//一点鎖線//線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.BLUE_2) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_V, 0.0f));
                            }//破線//線の太さや線の末端の形状
                            break;
                        case BLACK_TWO_DOT:
                            if (foldLines.getColor(i) == LineColor.BLACK_0) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
                            }//基本指定A　　線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.RED_1) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_M2, 0.0f));
                            }//二点鎖線//線の太さや線の末端の形状
                            if (foldLines.getColor(i) == LineColor.BLUE_2) {
                                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash_V, 0.0f));
                            }//破線//線の太さや線の末端の形状
                            break;
                    }

                    s_tv.set(camera.object2TV(foldLines.get(i)));
                    a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
                    b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

                    g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線

                    if (fWireFrameLineWidth < 2.0f) {//頂点の黒い正方形を描く
                        g.setColor(Color.black);
                        int i_width = pointSize;
                        g.fillRect((int) a.getX() - i_width, (int) a.getY() - i_width, 2 * i_width + 1, 2 * i_width + 1); //正方形を描く//g.fillRect(10, 10, 100, 50);長方形を描く
                        g.fillRect((int) b.getX() - i_width, (int) b.getY() - i_width, 2 * i_width + 1, 2 * i_width + 1); //正方形を描く
                    }


                    if (fWireFrameLineWidth >= 2.0f) {//  太線
                        g2.setStroke(new BasicStroke(1.0f + fWireFrameLineWidth % 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//線の太さや線の末端の形状、ここでは折線の端点の線の形状の指定
                        if (pointSize != 0) {
                            double d_haba = (double) fWireFrameLineWidth / 2.0 + (double) pointSize;


                            g.setColor(Color.white);
                            g2.fill(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                            g.setColor(Color.black);
                            g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                            g.setColor(Color.white);
                            g2.fill(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));

                            g.setColor(Color.black);
                            g2.draw(new Ellipse2D.Double(b.getX() - d_haba, b.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
                        }

                    }
                }
            }
        }

        //i_mouse_modeA==61//長方形内選択（paintの選択に似せた選択機能）の時に使う
        if (app.i_mouse_modeA == MouseMode.OPERATION_FRAME_CREATE_61) {
            origami_editor.graphic2d.point.Point p1 = new origami_editor.graphic2d.point.Point();
            p1.set(camera.TV2object(operationFrame_p1));
            origami_editor.graphic2d.point.Point p2 = new origami_editor.graphic2d.point.Point();
            p2.set(camera.TV2object(operationFrame_p2));
            origami_editor.graphic2d.point.Point p3 = new origami_editor.graphic2d.point.Point();
            p3.set(camera.TV2object(operationFrame_p3));
            origami_editor.graphic2d.point.Point p4 = new origami_editor.graphic2d.point.Point();
            p4.set(camera.TV2object(operationFrame_p4));

            line_step[1].set(p1, p2); //縦線
            line_step[2].set(p2, p3); //横線
            line_step[3].set(p3, p4); //縦線
            line_step[4].set(p4, p1); //横線

            line_step[1].setColor(LineColor.GREEN_6);
            line_step[2].setColor(LineColor.GREEN_6);
            line_step[3].setColor(LineColor.GREEN_6);
            line_step[4].setColor(LineColor.GREEN_6);
        }

        //線分入力時の一時的なs_step線分を描く　

        if ((app.i_mouse_modeA == MouseMode.OPERATION_FRAME_CREATE_61) && (i_drawing_stage != 4)) {
        } else {
            for (int i = 1; i <= i_drawing_stage; i++) {
                g_setColor(g, line_step[i].getColor());
                g2.setStroke(new BasicStroke(fWireFrameLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A　　線の太さや線の末端の形状

                s_tv.set(camera.object2TV(line_step[i]));
                a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
                b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため


                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線
                int i_haba_nyuiiryokuji = 3;
                if (i_kou_mitudo_nyuuryoku) {
                    i_haba_nyuiiryokuji = 2;
                }

                if (line_step[i].getActive() == LineSegment.ActiveState.ACTIVE_A_1) {
                    g.fillOval((int) a.getX() - i_haba_nyuiiryokuji, (int) a.getY() - i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji); //円
                }
                if (line_step[i].getActive() == LineSegment.ActiveState.ACTIVE_B_2) {
                    g.fillOval((int) b.getX() - i_haba_nyuiiryokuji, (int) b.getY() - i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji); //円
                }
                if (line_step[i].getActive() == LineSegment.ActiveState.ACTIVE_BOTH_3) {
                    g.fillOval((int) a.getX() - i_haba_nyuiiryokuji, (int) a.getY() - i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji); //円
                    g.fillOval((int) b.getX() - i_haba_nyuiiryokuji, (int) b.getY() - i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji, 2 * i_haba_nyuiiryokuji); //円
                }
            }
        }
        //候補入力時の候補を描く//System.out.println("_");
        g2.setStroke(new BasicStroke(fWireFrameLineWidth + 0.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));//基本指定A


        for (int i = 1; i <= i_candidate_stage; i++) {
            g_setColor(g, line_candidate[i].getColor());

            s_tv.set(camera.object2TV(line_candidate[i]));
            a.set(s_tv.getAX() + 0.000001, s_tv.getAY() + 0.000001);
            b.set(s_tv.getBX() + 0.000001, s_tv.getBY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

            g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY()); //直線
            int i_haba = pointSize + 5;

            if (line_candidate[i].getActive() == LineSegment.ActiveState.ACTIVE_A_1) {
                g.drawLine((int) a.getX() - i_haba, (int) a.getY(), (int) a.getX() + i_haba, (int) a.getY()); //直線
                g.drawLine((int) a.getX(), (int) a.getY() - i_haba, (int) a.getX(), (int) a.getY() + i_haba); //直線
            }
            if (line_candidate[i].getActive() == LineSegment.ActiveState.ACTIVE_B_2) {
                g.drawLine((int) b.getX() - i_haba, (int) b.getY(), (int) b.getX() + i_haba, (int) b.getY()); //直線
                g.drawLine((int) b.getX(), (int) b.getY() - i_haba, (int) b.getX(), (int) b.getY() + i_haba); //直線
            }
            if (line_candidate[i].getActive() == LineSegment.ActiveState.ACTIVE_BOTH_3) {
                g.drawLine((int) a.getX() - i_haba, (int) a.getY(), (int) a.getX() + i_haba, (int) a.getY()); //直線
                g.drawLine((int) a.getX(), (int) a.getY() - i_haba, (int) a.getX(), (int) a.getY() + i_haba); //直線

                g.drawLine((int) b.getX() - i_haba, (int) b.getY(), (int) b.getX() + i_haba, (int) b.getY()); //直線
                g.drawLine((int) b.getX(), (int) b.getY() - i_haba, (int) b.getX(), (int) b.getY() + i_haba); //直線
            }
        }

        g.setColor(Color.black);

        //円入力時の一時的な線分を描く　
        for (int i = 1; i <= i_circle_drawing_stage; i++) {
            g_setColor(g, circle_step[i].getColor());
            a.set(camera.object2TV(circle_step[i].getCenter()));//この場合のs_tvは描画座標系での円の中心の位置
            a.set(a.getX() + 0.000001, a.getY() + 0.000001);//なぜ0.000001を足すかというと,ディスプレイに描画するとき元の折線が新しい折線に影響されて動いてしまうのを防ぐため

            double d_haba = circle_step[i].getRadius() * camera.getCameraZoomX();//d_habaは描画時の円の半径。なお、camera.get_camera_bairitsu_x()＝camera.get_camera_bairitsu_y()を前提としている。

            g2.draw(new Ellipse2D.Double(a.getX() - d_haba, a.getY() - d_haba, 2.0 * d_haba, 2.0 * d_haba));
        }

        g.setColor(Color.black);

        if (i_bun_display) {
            g.drawString(text_cp_setumei, 10, 55);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------
    public void g_setColor(Graphics g, LineColor i) {
        switch (i) {
            case BLACK_0:
                g.setColor(Color.black);
                break;
            case RED_1:
                g.setColor(Color.red);
                break;
            case BLUE_2:
                g.setColor(Color.blue);
                break;
            case CYAN_3:
                g.setColor(new Color(100, 200, 200));
                break;
            case ORANGE_4:
                g.setColor(Color.orange);
                break;
            case MAGENTA_5:
                g.setColor(Color.magenta);
                break;
            case GREEN_6:
                g.setColor(Color.green);
                break;
            case YELLOW_7:
                g.setColor(Color.yellow);
                break;
            case PURPLE_8:
                g.setColor(new Color(210, 0, 255));
                break;
        }
    }


    //動作モデル00a--------------------------------------------------------------------------------------------------------
    //マウスクリック（マウスの近くの既成点を選択）、マウスドラッグ（選択した点とマウス間の線が表示される）、マウスリリース（マウスの近くの既成点を選択）してから目的の処理をする雛形セット

    public void set_i_en_egaki_dankai(int i) {
        i_circle_drawing_stage = i;
    }

    public void set_id_kakudo_kei(int i) {
        id_angle_system = i;
    }

    // ------------------------------------
    public void set_i_kou_mitudo_nyuuryoku(boolean i) {
        i_kou_mitudo_nyuuryoku = i;
    }

    public void addCircle(Circle e0) {
        addCircle(e0.getX(), e0.getY(), e0.getRadius(), e0.getColor());
    }


    //動作モデル00b--------------------------------------------------------------------------------------------------------
    //マウスクリック（近くの既成点かマウス位置を選択）、マウスドラッグ（選択した点とマウス間の線が表示される）、マウスリリース（近くの既成点かマウス位置を選択）してから目的の処理をする雛形セット

    public void addCircle(origami_editor.graphic2d.point.Point t0, double dr, LineColor ic) {
        addCircle(t0.getX(), t0.getY(), dr, ic);
    }

    public void addCircle(double dx, double dy, double dr, LineColor ic) {
        foldLines.addCircle(dx, dy, dr, ic);

        int imin = 1;
        int imax = foldLines.numCircles() - 1;
        int jmin = foldLines.numCircles();
        int jmax = foldLines.numCircles();

        foldLines.circle_circle_intersection(imin, imax, jmin, jmax);
        foldLines.lineSegment_circle_intersection(1, foldLines.getTotal(), jmin, jmax);

    }

    //--------------------------
    public void addLineSegment_auxiliary(LineSegment s0) {
        auxLines.addLine(s0);
    }

    //--------------------------------------------
    public void addLineSegment(LineSegment s0) {//0 = No change, 1 = Color change only, 2 = Line segment added
        foldLines.addLine(s0);//Just add the information of s0 to the end of senbun of foldLines
        int total_old = foldLines.getTotal();
        foldLines.lineSegment_circle_intersection(foldLines.getTotal(), foldLines.getTotal(), 1, foldLines.numCircles());

        foldLines.intersect_divide(1, total_old - 1, total_old, total_old);
    }


//--------------------------------------------
//28 28 28 28 28 28 28 28  i_mouse_modeA==28線分内分入力
    //動作概要
    //i_mouse_modeA==1と線分内分以外は同じ

    public origami_editor.graphic2d.point.Point getClosestPoint(origami_editor.graphic2d.point.Point t0) {
        // When dividing paper 1/1 Only the end point of the folding line is the reference point. The grid point never becomes the reference point.
        // When dividing paper from 1/2 to 1/512 The end point of the polygonal line and the grid point in the paper frame (-200.0, -200.0 _ 200.0, 200.0) are the reference points.
        origami_editor.graphic2d.point.Point t1 = new origami_editor.graphic2d.point.Point(); //End point of the polygonal line
        origami_editor.graphic2d.point.Point t3 = new origami_editor.graphic2d.point.Point(); //Center of circle

        t1.set(foldLines.closestPoint(t0)); // foldLines.closestPoint returns (100000.0,100000.0) if there is no close point

        t3.set(foldLines.closestCenter(t0)); // foldLines.closestCenter returns (100000.0,100000.0) if there is no close point

        if (t0.distanceSquared(t1) > t0.distanceSquared(t3)) {
            t1.set(t3);
        }

        if (grid.getBaseState() == Grid.State.HIDDEN) {
            return t1;
        }

        if (t0.distanceSquared(t1) > t0.distanceSquared(grid.closestGridPoint(t0))) {
            return grid.closestGridPoint(t0);
        }

        return t1;
    }

    //------------------------------
    public LineSegment getClosestLineSegment(origami_editor.graphic2d.point.Point t0) {
        return foldLines.closestLineSegment(t0);
    }

    //------------------------------------------------------
    public LineSegment get_moyori_step_senbun(origami_editor.graphic2d.point.Point t0, int imin, int imax) {
        int minrid = -100;
        double minr = 100000;//Senbun s1 =new Senbun(100000.0,100000.0,100000.0,100000.1);
        for (int i = imin; i <= imax; i++) {
            double sk = OritaCalc.distance_lineSegment(t0, line_step[i]);
            if (minr > sk) {
                minr = sk;
                minrid = i;
            }//柄の部分に近いかどうか

        }

        return line_step[minrid];
    }


//1 1 1 1 1 1 01 01 01 01 01 11111111111 i_mouse_modeA==1線分入力 111111111111111111111111111111111
    //動作概要　
    //マウスボタン押されたとき　
    //用紙1/1分割時 		折線の端点のみが基準点。格子点が基準点になることはない。
    //用紙1/2から1/512分割時	折線の端点と用紙枠内（-200.0,-200.0 _ 200.0,200.0)）の格子点とが基準点
    //入力点Pが基準点から格子幅kus.d_haba()の1/4より遠いときは折線集合への入力なし
    //線分が長さがなく1点状のときは折線集合への入力なし

    //------------------------------
    public Circle get_moyori_ensyuu(origami_editor.graphic2d.point.Point t0) {
        return foldLines.closestCircleMidpoint(t0);
    }

    //------------------------------------------------------
    public Circle get_moyori_step_ensyuu(origami_editor.graphic2d.point.Point t0, int imin, int imax) {
        int minrid = -100;
        double minr = 100000;
        for (int i = imin; i <= imax; i++) {
            double ek = OritaCalc.distance_circumference(t0, circle_step[i]);
            if (minr > ek) {
                minr = ek;
                minrid = i;
            }//円周に近いかどうか
        }
        return circle_step[minrid];
    }

    public void set_s_step_iactive(LineSegment.ActiveState ia) {
        for (int i = 0; i < 1024; i++) {
            line_step[i].setActive(ia);
        }
    }

    //動作モデル001--------------------------------------------------------------------------------------------------------
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_m_001(origami_editor.graphic2d.point.Point p0, LineColor i_c) {//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点が候補点となる。近くに既成の点が無いときは候補点無しなので候補点の表示も無し。
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
            i_candidate_stage = 0;
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_candidate_stage = 1;
                line_candidate[1].set(closest_point, closest_point);
                line_candidate[1].setColor(i_c);
            }
        }
    }

    //動作モデル002--------------------------------------------------------------------------------------------------------
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_m_002(origami_editor.graphic2d.point.Point p0, LineColor i_c) {//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            line_candidate[1].setColor(i_c);
        }
    }

    //動作モデル003--------------------------------------------------------------------------------------------------------
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_m_003(origami_editor.graphic2d.point.Point p0, LineColor i_c) {//マウスで選択できる候補点を表示する。常にマウスの位置自身が候補点となる。
        if (i_kou_mitudo_nyuuryoku) {
            //line_candidate[1].setiactive(3);
            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            line_candidate[1].set(p, p);

            line_candidate[1].setColor(i_c);
        }
    }

    //マウスを動かしたとき----------------------------------------------
    public void mMoved_m_00a(origami_editor.graphic2d.point.Point p0, LineColor i_c) {
        mMoved_m_001(p0, i_c);
    }//近い既存点のみ表示

    //マウスクリック----------------------------------------------------
    public void mPressed_m_00a(origami_editor.graphic2d.point.Point p0, LineColor i_c) {
        i_drawing_stage = 1;
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(i_c);
    }

    //マウスドラッグ---------------------------------------------------
    public void mDragged_m_00a(origami_editor.graphic2d.point.Point p0, LineColor i_c) {  //近い既存点のみ表示

        p.set(camera.TV2object(p0));
        line_step[1].setA(p);

        if (i_kou_mitudo_nyuuryoku) {
            i_candidate_stage = 0;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_candidate_stage = 1;
                line_candidate[1].set(closest_point, closest_point);
                line_candidate[1].setColor(i_c);
                line_step[1].setA(line_candidate[1].getA());
            }
        }
    }

// ------------------------------------------

    //マウスを動かしたとき----------------------------------------------
    public void mMoved_m_00b(origami_editor.graphic2d.point.Point p0, LineColor i_c) {
        mMoved_m_002(p0, i_c);
    }//近くの既成点かマウス位置表示

    //マウスクリック----------------------------------------------------
    public void mPressed_m_00b(origami_editor.graphic2d.point.Point p0, LineColor i_c) {
        i_drawing_stage = 1;
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        p.set(camera.TV2object(p0));
        line_step[1].set(p, p);

        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            line_step[1].set(p, closest_point);
        }

        line_step[1].setColor(i_c);
    }

    //マウスドラッグ---------------------------------------------------
    public void mDragged_m_00b(origami_editor.graphic2d.point.Point p0, LineColor i_c) {  //近くの既成点かマウス位置表示

        p.set(camera.TV2object(p0));
        line_step[1].setA(p);

        if (i_kou_mitudo_nyuuryoku) {
            closest_point.set(getClosestPoint(p));
            i_candidate_stage = 1;
            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }
            line_candidate[1].setColor(i_c);
            line_step[1].setA(line_candidate[1].getA());
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_28(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00a(p0, lineColor);//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    }

    //マウス操作(i_mouse_modeA==28線分内分入力 でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_28(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            line_step[1].set(p, closest_point);
            line_step[1].setColor(lineColor);
            return;
        }
        line_step[1].set(p, p);
        line_step[1].setColor(lineColor);
    }

    //マウス操作(i_mouse_modeA==28線分入力 でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_28(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);

        if (i_kou_mitudo_nyuuryoku) {
            closest_point.set(getClosestPoint(p));
            i_candidate_stage = 1;
            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }
            line_candidate[1].setColor(lineColor);
            line_step[1].setA(line_candidate[1].getA());
        }
    }

    //マウス操作(i_mouse_modeA==28線分入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_28(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));

        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));

        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            if ((d_internalDivisionRatio_s == 0.0) && (d_internalDivisionRatio_t == 0.0)) {
            }
            if ((d_internalDivisionRatio_s == 0.0) && (d_internalDivisionRatio_t != 0.0)) {
                addLineSegment(line_step[1]);
            }
            if ((d_internalDivisionRatio_s != 0.0) && (d_internalDivisionRatio_t == 0.0)) {
                addLineSegment(line_step[1]);
            }
            if ((d_internalDivisionRatio_s != 0.0) && (d_internalDivisionRatio_t != 0.0)) {
                LineSegment s_ad = new LineSegment();
                s_ad.setColor(lineColor);
                double nx = (d_internalDivisionRatio_t * line_step[1].getBX() + d_internalDivisionRatio_s * line_step[1].getAX()) / (d_internalDivisionRatio_s + d_internalDivisionRatio_t);
                double ny = (d_internalDivisionRatio_t * line_step[1].getBY() + d_internalDivisionRatio_s * line_step[1].getAY()) / (d_internalDivisionRatio_s + d_internalDivisionRatio_t);
                s_ad.set(line_step[1].getAX(), line_step[1].getAY(), nx, ny);
                addLineSegment(s_ad);
                s_ad.set(line_step[1].getBX(), line_step[1].getBY(), nx, ny);
                addLineSegment(s_ad);
            }
            record();
        }
    }


//------------------------------

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_01(origami_editor.graphic2d.point.Point p0) {
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
                line_candidate[1].setColor(lineColor);
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
                line_candidate[1].setColor(auxLineColor);
            }

        }
    }


    // -----------------------------------------------

    //マウス操作(i_mouse_modeA==1線分入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_01(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        p.set(camera.TV2object(p0));

        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            line_step[1].set(p, closest_point);
            if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
                line_step[1].setColor(lineColor);
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
                line_step[1].setColor(auxLineColor);
            }
            return;
        }

        line_step[1].set(p, p);
        if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
            line_step[1].setColor(lineColor);
        }
        if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
            line_step[1].setColor(auxLineColor);
        }
    }

    // --------------------------------------------

    //マウス操作(i_mouse_modeA==1線分入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_01(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        if (!i_kou_mitudo_nyuuryoku) {
            line_step[1].setA(p);
        }

        if (i_kou_mitudo_nyuuryoku) {
            closest_point.set(getClosestPoint(p));
            i_candidate_stage = 1;
            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
                line_candidate[1].setColor(lineColor);
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
                line_candidate[1].setColor(auxLineColor);
            }
            line_step[1].setA(line_candidate[1].getA());
        }
    }


    //-----------------------------------------------62ここまで　//20181121　iactiveをtppに置き換える


//-------------------------------------------------------------------------------------------------------

//--------------------------------------

    public origami_editor.graphic2d.point.Point get_moyori_ten_sisuu(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        return new origami_editor.graphic2d.point.Point(grid.getIndex(closest_point));
    }

    //マウス操作(i_mouse_modeA==1線分入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_01(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
                addLineSegment(line_step[1]);
                record();
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
                addLineSegment_auxiliary(line_step[1]);
                h_record();
            }
        }
    }

    //11 11 11 11 11 11 11 11 11 11 11
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_11(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00a(p0, lineColor);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==11線分入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_11(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00a(p0, lineColor);
    }

//------

    //マウス操作(i_mouse_modeA==11線分入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_11(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00a(p0, lineColor);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==11線分入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_11(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;

            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {
                    addLineSegment(line_step[1]);
                    record();
                }
            }
        }
    }

    //Function to operate the mouse (i_mouse_modeA == 62 Voronoi when the mouse is moved)
    public void mMoved_A_62(origami_editor.graphic2d.point.Point p0) {
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
                line_candidate[1].setColor(lineColor);
            }
            if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
                line_candidate[1].setColor(auxLineColor);
            }

        }
    }

    // ------------------------------------------------------------------------------------------------------------
    int s_step_no_1_top_continue_no_point_no_number() {//line_step [i] returns the number of Point (length 0) from the beginning. Returns 0 if there are no dots
        int r_i = 0;
        int i_add = 1;
        for (int i = 1; i <= i_drawing_stage; i++) {
            if (line_step[i].getLength() > 0.00000001) {
                i_add = 0;
            }
            r_i = r_i + i_add;
        }
        return r_i;
    }


//-------------------------------------------------------------------------------------------------------------------------------

    //マウス操作(i_mouse_modeA==62ボロノイ　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_62(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        //Arranged i_drawing_stage to be only the conventional Voronoi mother point (yet, we have not decided whether to add the point p as line_step to the Voronoi mother point)
        i_drawing_stage = s_step_no_1_top_continue_no_point_no_number();//Tenの数

        //Find the point-like line segment s_temp consisting of the closest points of p newly added at both ends (if there is no nearest point, both ends of s_temp are p)
        LineSegment s_temp = new LineSegment();
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            s_temp.set(closest_point, closest_point);
            s_temp.setColor(LineColor.MAGENTA_5);
        } else {
            s_temp.set(p, p);
            s_temp.setColor(LineColor.MAGENTA_5);
        }


        //Confirm that the newly added p does not overlap with the previously added Ten
        i_mouse_modeA_62_point_overlapping = 0;

        for (int i = 1; i <= i_drawing_stage; i++) {
            if (OritaCalc.distance(line_step[i].getA(), s_temp.getA()) <= d_decision_width) {
                i_mouse_modeA_62_point_overlapping = i;
            }
        }

        //Confirm that the newly added p does not overlap with the previously added Point.

        if (i_mouse_modeA_62_point_overlapping == 0) {

            //(ここでやっと、点pをs_stepとしてボロノイ母点に加えると決まった)
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(s_temp);
            line_step[i_drawing_stage].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);//Circles are drawn at both ends of the line with iactive = 3. For the line of iactive = 1, a circle is drawn only at the a end. The line of iactive = 2 has a circle drawn only at the b end

            //今までのボロノイ図を元に、１個の新しいボロノイ母点を加えたボロノイ図を作る--------------------------------------

            //voronoi_01();//低速、エラーはほとんどないはず
            voronoi_02();//Fast, maybe there are still errors
        } else {//Removed Voronoi mother points with order i_mouse_modeA_62_point_overlapping
            //順番がi_mouse_modeA_62_ten_kasanariのボロノイ母点と順番が最後(=i_egaki_dankai)のボロノイ母点を入れ替える
            //line_step[i]の入れ替え
            LineSegment S_replace = new LineSegment();
            S_replace.set(line_step[i_mouse_modeA_62_point_overlapping]);
            line_step[i_mouse_modeA_62_point_overlapping].set(line_step[i_drawing_stage]);
            line_step[i_drawing_stage].set(S_replace);


            for (int j = 1; j <= vonoroiLines.getTotal(); j++) {
                //Swapping the vonoroiA of the line segment in vonoroiLines
                if (vonoroiLines.getVonoroiA(j) == i_mouse_modeA_62_point_overlapping) {
                    vonoroiLines.setVonoroiA(j, i_drawing_stage);
                } else if (vonoroiLines.getVonoroiA(j) == i_drawing_stage) {
                    vonoroiLines.setVonoroiA(j, i_mouse_modeA_62_point_overlapping);
                }

                //Replacing the vonoroiB of the line segment in vonoroiLines
                if (vonoroiLines.getVonoroiB(j) == i_mouse_modeA_62_point_overlapping) {
                    vonoroiLines.setVonoroiB(j, i_drawing_stage);
                } else if (vonoroiLines.getVonoroiB(j) == i_drawing_stage) {
                    vonoroiLines.setVonoroiB(j, i_mouse_modeA_62_point_overlapping);
                }
            }


            //Deleted the Voronoi mother point of the last order (= i_drawing_stage)

            i_drawing_stage = i_drawing_stage - 1;

            FoldLineSet ori_v_temp = new FoldLineSet();    //修正用のボロノイ図の線を格納する

            //Deselect all vonoroiLines line segments first
            vonoroiLines.unselect_all();

            //i_egaki_dankai+1のボロノイ母点からのボロノイ線分を選択状態にする
            LineSegment s_tem = new LineSegment();
            LineSegment s_tem2 = new LineSegment();
            for (int j = 1; j <= vonoroiLines.getTotal(); j++) {
                s_tem.set(vonoroiLines.get(j));//s_temとしてボロノイ母点からのボロノイ線分か判定
                if (s_tem.getVonoroiA() == i_drawing_stage + 1) {//The two Voronoi vertices of the Voronoi line segment are recorded in vonoroiA and vonoroiB.
                    vonoroiLines.select(j);
                    for (int h = 1; h <= vonoroiLines.getTotal(); h++) {
                        s_tem2.set(vonoroiLines.get(h));
                        if (s_tem.getVonoroiB() == s_tem2.getVonoroiB()) {
                            vonoroiLines.select(h);
                        }
                        if (s_tem.getVonoroiB() == s_tem2.getVonoroiA()) {
                            vonoroiLines.select(h);
                        }
                    }


                    //削除されるi_egaki_dankai+1番目のボロノイ母点と組になる、もう一つのボロノイ母点を取り囲むボロノイ線分のアレイリストを得る。
                    Senb_boro_1p_motome(s_tem.getVonoroiB());

                    for (LineSegment lineSegment : lineSegment_vonoroi_onePoint) {
                        LineSegment add_S = new LineSegment();
                        add_S.set(lineSegment);
                        LineSegment add_S2 = new LineSegment();


                        //Pre-check whether to add add_S to ori_v_temp
                        int i_tuika = 1;//1なら追加する。0なら追加しない。
                        for (int h = 1; h <= ori_v_temp.getTotal(); h++) {
                            add_S2.set(ori_v_temp.get(h));
                            if ((add_S.getVonoroiB() == add_S2.getVonoroiB()) && (add_S.getVonoroiA() == add_S2.getVonoroiA())) {
                                i_tuika = 0;
                            }
                            if ((add_S.getVonoroiB() == add_S2.getVonoroiA()) && (add_S.getVonoroiA() == add_S2.getVonoroiB())) {
                                i_tuika = 0;
                            }
                        }
                        //ori_v_tempにadd_Sを追加するかどうかの事前チェックはここまで

                        if (i_tuika == 1) {
                            ori_v_temp.addLine(lineSegment);
                        }
                    }
                } else if (s_tem.getVonoroiB() == i_drawing_stage + 1) {//The two Voronoi vertices of the Voronoi line segment are recorded in iactive and color.
                    vonoroiLines.select(j);
                    for (int h = 1; h <= vonoroiLines.getTotal(); h++) {
                        s_tem2.set(vonoroiLines.get(h));
                        if (s_tem.getVonoroiA() == s_tem2.getVonoroiB()) {
                            vonoroiLines.select(h);
                        }
                        if (s_tem.getVonoroiA() == s_tem2.getVonoroiA()) {
                            vonoroiLines.select(h);
                        }
                    }

                    //削除されるi_egaki_dankai+1番目のボロノイ母点と組になる、もう一つのボロノイ母点を取り囲むボロノイ線分のアレイリストを得る。
                    Senb_boro_1p_motome(s_tem.getVonoroiA());

                    for (LineSegment lineSegment : lineSegment_vonoroi_onePoint) {
                        LineSegment add_S = new LineSegment();
                        add_S.set(lineSegment);
                        LineSegment add_S2 = new LineSegment();

                        //ori_v_tempにadd_Sを追加するかどうかの事前チェック
                        int i_tuika = 1;//1なら追加する。0なら追加しない。
                        for (int h = 1; h <= ori_v_temp.getTotal(); h++) {
                            add_S2.set(ori_v_temp.get(h));
                            if ((add_S.getVonoroiB() == add_S2.getVonoroiB()) && (add_S.getVonoroiA() == add_S2.getVonoroiA())) {
                                i_tuika = 0;
                            }
                            if ((add_S.getVonoroiB() == add_S2.getVonoroiA()) && (add_S.getVonoroiA() == add_S2.getVonoroiB())) {
                                i_tuika = 0;
                            }
                        }
                        //This is the end of the pre-check whether to add add_S to ori_v_temp

                        if (i_tuika == 1) {
                            ori_v_temp.addLine(lineSegment);
                        }
                    }
                }
            }
            //選択状態のものを削除
            vonoroiLines.delSelectedLineSegmentFast();
            vonoroiLines.del_V_all(); //You may not need this line

            //ori_v_tempのボロノイ線分をボロノイ母点に加える
            //ori_v_temp.hyouji("ori_v_temp---------------------");
            for (int j = 1; j <= ori_v_temp.getTotal(); j++) {
                LineSegment s_t = new LineSegment();
                s_t.set(ori_v_temp.get(j));
                vonoroiLines.addLine(s_t);
            }

            vonoroiLines.del_V_all();

        }


        //ボロノイ図も表示するようにs_stepの後にボロノイ図の線を入れる

        int imax = vonoroiLines.getTotal();
        if (imax > 1020) {
            imax = 1020;
        }

        //System.out.println("ボロノイ図も表示するようにs_stepの後にボロノイ図の線を入れる前");
        //System.out.println("i_egaki_dankai="+i_egaki_dankai+" :  vonoroiLines.getsousuu()= "+vonoroiLines.getsousuu());

        for (int i = 1; i <= imax; i++) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(vonoroiLines.get(i));
            //line_step[i_egaki_dankai].setiactive(3);
            line_step[i_drawing_stage].setActive(LineSegment.ActiveState.INACTIVE_0);
            line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
        }


    }

    //--------------------------------------------
    public void addLineSegmentVonoroi(LineSegment s0) {

        vonoroiLines.addLine(s0);//ori_vのsenbunの最後にs0の情報をを加えるだけ
        int sousuu_old = vonoroiLines.getTotal();
        vonoroiLines.lineSegment_circle_intersection(vonoroiLines.getTotal(), vonoroiLines.getTotal(), 1, vonoroiLines.numCircles());

        vonoroiLines.intersect_divide(1, sousuu_old - 1, sousuu_old, sousuu_old);
    }

    // -----------------------------------------------------------------------------
    //マウス操作(i_mouse_modeA==62ボロノイ　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_62(origami_editor.graphic2d.point.Point p0) {
    }

    // -----------------------------------------------------------------------------
    //マウス操作(i_mouse_modeA==62ボロノイ　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_62(origami_editor.graphic2d.point.Point p0) {
    }

//--------------------------------------


//71 71 71 71 71 71 71 71 71 71 71 71 71 71    i_mouse_modeA==71　;線分延長モード

    public void voronoi_02_01(int tyuusinn_ten_bangou, LineSegment add_lineSegment) {
        //i_egaki_dankai番目のボロノイ頂点は　　line_step[i_egaki_dankai].geta()　　　

        //Organize the line segments to be added
        StraightLine add_straightLine = new StraightLine(add_lineSegment);

        int i_saisyo = lineSegment_vonoroi_onePoint.size() - 1;
        for (int i = i_saisyo; i >= 0; i--) {
            //Organize existing line segments
            LineSegment existing_lineSegment = new LineSegment();
            existing_lineSegment.set(lineSegment_vonoroi_onePoint.get(i));
            StraightLine existing_straightLine = new StraightLine(existing_lineSegment);

            //Fight the line segment to be added with the existing line segment

            OritaCalc.ParallelJudgement parallel = OritaCalc.parallel_judgement(add_straightLine, existing_straightLine, 0.0001);//0 = not parallel, 1 = parallel and 2 straight lines do not match, 2 = parallel and 2 straight lines match

            if (parallel == OritaCalc.ParallelJudgement.NOT_PARALLEL) {//When the line segment to be added and the existing line segment are non-parallel
                origami_editor.graphic2d.point.Point intersection = new origami_editor.graphic2d.point.Point();
                intersection.set(OritaCalc.findIntersection(add_straightLine, existing_straightLine));

                if ((add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getA()) <= 0) &&
                        (add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getB()) <= 0)) {
                    lineSegment_vonoroi_onePoint.remove(i);
                } else if ((add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getA()) == 1) &&
                        (add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getB()) == -1)) {
                    existing_lineSegment.set(existing_lineSegment.getA(), intersection);
                    if (existing_lineSegment.getLength() < 0.0000001) {
                        lineSegment_vonoroi_onePoint.remove(i);
                    } else {
                        lineSegment_vonoroi_onePoint.set(i, existing_lineSegment);
                    }
                } else if ((add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getA()) == -1) &&
                        (add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getB()) == 1)) {
                    existing_lineSegment.set(intersection, existing_lineSegment.getB());
                    if (existing_lineSegment.getLength() < 0.0000001) {
                        lineSegment_vonoroi_onePoint.remove(i);
                    } else {
                        lineSegment_vonoroi_onePoint.set(i, existing_lineSegment);
                    }
                }

                //

                if ((existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getA()) <= 0) &&
                        (existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getB()) <= 0)) {
                    return;
                } else if ((existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getA()) == 1) &&
                        (existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getB()) == -1)) {
                    add_lineSegment.set(add_lineSegment.getA(), intersection);
                    if (add_lineSegment.getLength() < 0.0000001) {
                        return;
                    }
                } else if ((existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getA()) == -1) &&
                        (existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getB()) == 1)) {
                    add_lineSegment.set(intersection, add_lineSegment.getB());
                    if (add_lineSegment.getLength() < 0.0000001) {
                        return;
                    }
                }


            } else if (parallel == OritaCalc.ParallelJudgement.PARALLEL_NOT_EQUAL) {//When the line segment to be added and the existing line segment are parallel and the two straight lines do not match
                if (add_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), existing_lineSegment.getA()) == -1) {
                    lineSegment_vonoroi_onePoint.remove(i);
                } else if (existing_straightLine.sameSide(line_step[tyuusinn_ten_bangou].getA(), add_lineSegment.getA()) == -1) {
                    return;
                }


            } else if (parallel == OritaCalc.ParallelJudgement.PARALLEL_EQUAL) {//When the line segment to be added and the existing line segment are parallel and the two straight lines match
                return;
            }
        }

        lineSegment_vonoroi_onePoint.add(add_lineSegment);
    }

    public void Senb_boro_1p_motome(int center_point_count) {//It can be used when line_step contains only Voronoi mother points. Get Senb_boro_1p as a set of Voronoi line segments around center_point_count
        //i_egaki_dankai Obtain an array list of Voronoi line segments surrounding the third Voronoi vertex. // i_egaki_dankai The third Voronoi apex is line_step [i_egaki_dankai] .geta ()
        lineSegment_vonoroi_onePoint.clear();

        for (int i_e_d = 1; i_e_d <= i_drawing_stage; i_e_d++) {
            if (i_e_d != center_point_count) {
                //Find the line segment to add
                LineSegment add_lineSegment = new LineSegment();

                add_lineSegment.set(OritaCalc.bisection(line_step[i_e_d].getA(), line_step[center_point_count].getA(), 1000.0));

                System.out.println("center_point_count= " + center_point_count + " ,i_e_d= " + i_e_d);

                if (i_e_d < center_point_count) {
                    add_lineSegment.setVonoroiA(i_e_d);
                    add_lineSegment.setVonoroiB(center_point_count);//Record the two Voronoi vertices of the Voronoi line segment in iactive and color
                } else {
                    add_lineSegment.setVonoroiA(center_point_count);
                    add_lineSegment.setVonoroiB(i_e_d);//Record the two Voronoi vertices of the Voronoi line segment in iactive and color
                }
                voronoi_02_01(center_point_count, add_lineSegment);
            }
        }
    }

    public void voronoi_02() {//i=1からi_egaki_dankaiまでのs_step[i]と、i_egaki_dankai-1までのボロノイ図からi_egaki_dankaiのボロノイ図を作成

        //i_egaki_dankai番目のボロノイ頂点を取り囲むボロノイ線分のアレイリストを得る。
        Senb_boro_1p_motome(i_drawing_stage);

        //20181109ここでori_v.の既存のボロノイ線分の整理が必要

        //ori_vの線分を最初に全て非選択にする
        vonoroiLines.unselect_all();

        //
        LineSegment s_begin = new LineSegment();
        LineSegment s_end = new LineSegment();

        for (int ia = 0; ia < lineSegment_vonoroi_onePoint.size() - 1; ia++) {
            for (int ib = ia + 1; ib < lineSegment_vonoroi_onePoint.size(); ib++) {

                s_begin.set(lineSegment_vonoroi_onePoint.get(ia));
                s_end.set(lineSegment_vonoroi_onePoint.get(ib));

                StraightLine t_begin = new StraightLine(s_begin);

                int i_begin = s_begin.getVonoroiA();//In this case, vonoroiA contains the number of the existing Voronoi mother point when the Voronoi line segment is added.
                int i_end = s_end.getVonoroiA();//In this case, vonoroiA contains the number of the existing Voronoi mother point when the Voronoi line segment is added.


                if (i_begin > i_end) {
                    int i_temp = i_begin;
                    i_begin = i_end;
                    i_end = i_temp;
                }

                //The surrounding Voronoi line segment created by adding a new Voronoi matrix is being sought. The polygon of this Voronoi line segment is called a new cell.
                // Before adding a new cell to vonoroiLines, process so that there is no existing line segment of vonoroiLines that is inside the new cell.

                //20181109ここでori_v.の既存のボロノイ線分(iactive()が必ずicolorより小さくなっている)を探す
                for (int j = 1; j <= vonoroiLines.getTotal(); j++) {
                    LineSegment s_kizon = new LineSegment();
                    s_kizon.set(vonoroiLines.get(j));

                    int i_kizon_syou = s_kizon.getVonoroiA();
                    int i_kizon_dai = s_kizon.getVonoroiB();

                    if (i_kizon_syou > i_kizon_dai) {
                        i_kizon_dai = s_kizon.getVonoroiA();
                        i_kizon_syou = s_kizon.getVonoroiB();
                    }

                    if (i_kizon_syou == i_begin) {
                        if (i_kizon_dai == i_end) {

//20181110ここポイント
//
//	-1		0		1
//-1 	何もせず	何もせず	交点まで縮小
// 0	何もせず	有り得ない	削除
// 1	交点まで縮小	削除		削除
//

                            origami_editor.graphic2d.point.Point kouten = new origami_editor.graphic2d.point.Point();
                            kouten.set(OritaCalc.findIntersection(s_begin, s_kizon));

                            if ((t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getA()) >= 0) &&
                                    (t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getB()) >= 0)) {
                                vonoroiLines.select(j);
                            }

                            if ((t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getA()) == -1) &&
                                    (t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getB()) == 1)) {
                                vonoroiLines.set(j, s_kizon.getA(), kouten);
                            }

                            if ((t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getA()) == 1) &&
                                    (t_begin.sameSide(line_step[i_drawing_stage].getA(), s_kizon.getB()) == -1)) {
                                vonoroiLines.set(j, kouten, s_kizon.getB());
                            }
                        }
                    }
                }
            }
        }

        //for (int i=1; i<=vonoroiLines.getsousuu(); i++ ){System.out.println("    (1)  i= " + i +  ":  vonoroiLines.get(i).getiactive()=  " +  vonoroiLines.get(i).getiactive());}
        //選択状態のものを削除
        vonoroiLines.delSelectedLineSegmentFast();

        vonoroiLines.del_V_all(); //この行はいらないかも


        //Add the line segment of Senb_boro_1p to the end of senbun of vonoroiLines
        for (LineSegment lineSegment : lineSegment_vonoroi_onePoint) {
            LineSegment add_S = new LineSegment();
            add_S.set(lineSegment);
            vonoroiLines.addLine(lineSegment);
        }
    }

    //5 5 5 5 5 55555555555555555    i_mouse_modeA==5　;線分延長モード
    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_05(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_05or70(p0);
    }//常にマウスの位置のみが候補点

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_05(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_05or70(p0);
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_05(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_05or70(p0);
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_05(origami_editor.graphic2d.point.Point p0) {
        mReleased_A_05or70(p0);
    }





/*
//------折り畳み可能線+格子点系入力


//71 71 71 71 71 71 71    i_mouse_modeA==71　;折り畳み可能線入力  qqqqqqqqq
int i_step_for71=0;//i_step_for71=2の場合は、step線が1本だけになっていて、次の操作で入力折線が確定する状態
//
//課題　step線と既存折線が平行の時エラー方向に線を引くことを改善すること20170407
//
//動作仕様
//（１）点を選択（既存点選択規制）
//（２a）選択点が3以上の奇数折線の頂点の場合
//（３）
//
//
//（２b）２a以外の場合
//



	//マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");


	//マウス操作(ボタンを押したとき)時の作業--------------
	public void mPressed_A_71(Ten p0) {
		//Ten p =new Ten();
		p.set(camera.TV2object(p0));

		if(i_egaki_dankai==0){i_step_for71=0;}



		//if(i_egaki_dankai==0){i_step_for71=0;}

		if(i_step_for71==0){
			double hantei_kyori=0.000001;

			//任意の点が与えられたとき、端点もしくは格子点で最も近い点を得る
			moyori_ten.set(get_moyori_ten(p));

			if(p.kyori(moyori_ten)<d_decision_width){
				//i_egaki_dankai=i_egaki_dankai+1;
				//line_step[i_egaki_dankai].set(moyori_ten,moyori_ten);line_step[i_egaki_dankai].setcolor(i_egaki_dankai);

				//moyori_tenを端点とする折線をNarabebakoに入れる
				Narabebako_int_double nbox =new Narabebako_int_double();
				for (int i=1; i<=foldLines.getsousuu(); i++ ){ if((0<=foldLines.getcolor(i))&&(foldLines.getcolor(i)<=2)){
					if(moyori_ten.kyori(foldLines.geta(i))<hantei_kyori){
						nbox.ire_i_tiisaijyun(new int_double( i  , oc.angle(foldLines.geta(i),foldLines.getb(i)) ));
					}else if(moyori_ten.kyori(foldLines.getb(i))<hantei_kyori){
						nbox.ire_i_tiisaijyun(new int_double( i  , oc.angle(foldLines.getb(i),foldLines.geta(i)) ));
					}
				}}
				//System.out.println("nbox.getsousuu()="+nbox.getsousuu());
				if(nbox.getsousuu()%2==1){//moyori_tenを端点とする折線の数が奇数のときだけif{}内の処理をする
					//System.out.println("20170130_3");

					//int i_kouho_suu=0;
					for (int i=1; i<=nbox.getsousuu(); i++ ){//iは角加減値を求める最初の折線のid
						//折線が奇数の頂点周りの角加減値を2.0で割ると角加減値の最初折線と、折り畳み可能にするための追加の折線との角度になる。
						double kakukagenti=0.0;
						//System.out.println("nbox.getsousuu()="+nbox.getsousuu());
						int tikai_orisen_jyunban;
						int tooi_orisen_jyunban;
						for (int k=1; k<=nbox.getsousuu(); k++ ){//kは角加減値を求める角度の順番
							tikai_orisen_jyunban=i+k-1;if(tikai_orisen_jyunban>nbox.getsousuu()){tikai_orisen_jyunban=tikai_orisen_jyunban-nbox.getsousuu();}
							tooi_orisen_jyunban =i+k  ;if(tooi_orisen_jyunban >nbox.getsousuu()){tooi_orisen_jyunban =tooi_orisen_jyunban -nbox.getsousuu();}

							double add_kakudo=oc.kakudo_osame_0_360(nbox.get_double(tooi_orisen_jyunban)-nbox.get_double(tikai_orisen_jyunban));
							if(k%2==1){kakukagenti=kakukagenti+add_kakudo;
							}else if(k%2==0){kakukagenti=kakukagenti-add_kakudo;
							}
							//System.out.println("i="+i+"   k="+k);
						}

if(nbox.getsousuu()==1){kakukagenti=360.0;}
						//System.out.println("kakukagenti="+kakukagenti);
						//チェック用に角加減値の最初の角度の中にkakukagenti/2.0があるかを確認する
						tikai_orisen_jyunban=i  ;if(tikai_orisen_jyunban>nbox.getsousuu()){tikai_orisen_jyunban=tikai_orisen_jyunban-nbox.getsousuu();}
						tooi_orisen_jyunban =i+1;if(tooi_orisen_jyunban >nbox.getsousuu()){tooi_orisen_jyunban =tooi_orisen_jyunban -nbox.getsousuu();}

						double add_kakudo_1=oc.kakudo_osame_0_360(nbox.get_double(tooi_orisen_jyunban)-nbox.get_double(tikai_orisen_jyunban));
if(nbox.getsousuu()==1){add_kakudo_1=360.0;}

						if((kakukagenti/2.0>0.0+0.000001)&&(kakukagenti/2.0<add_kakudo_1-0.000001)){
							i_egaki_dankai=i_egaki_dankai+1;

							//線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d)
							Senbun s_kiso =new Senbun();
							if(moyori_ten.kyori(foldLines.geta(nbox.get_int(i)))<hantei_kyori){
								s_kiso.set(foldLines.geta(nbox.get_int(i)),foldLines.getb(nbox.get_int(i)));
							}else if(moyori_ten.kyori(foldLines.getb(nbox.get_int(i)))<hantei_kyori){
								s_kiso.set(foldLines.getb(nbox.get_int(i)),foldLines.geta(nbox.get_int(i)));
							}

							double s_kiso_nagasa=s_kiso.getnagasa();

							line_step[i_egaki_dankai].set(oc.Senbun_kaiten(s_kiso,kakukagenti/2.0,kus.d_haba()/s_kiso_nagasa) );
						 	line_step[i_egaki_dankai].setcolor(8);
							line_step[i_egaki_dankai].setiactive(1);

						}

					}
					//if(i_kouho_suu==1){i_step_for71=2;}
					//if(i_kouho_suu>1){i_step_for71=1;}

					if(i_egaki_dankai==1){i_step_for71=2;}
					if(i_egaki_dankai>1){i_step_for71=1;}
				}

				if(i_egaki_dankai==0){//折畳み可能化線がない場合//System.out.println("_");
					i_egaki_dankai=1;
					i_step_for71=1;
					line_step[1].set(moyori_ten,moyori_ten);
				 	line_step[1].setcolor(8);
					line_step[1].setiactive(3);
				}

			}
			return;
		}



		if(i_step_for71==1){
			moyori_senbun.set(get_moyori_step_senbun(p,1,i_egaki_dankai));
			if((i_egaki_dankai>=2)&&(oc.kyori_senbun( p,moyori_senbun)<d_decision_width)){
			//if(oc.kyori_senbun( p,moyori_senbun)<d_decision_width){
				//System.out.println("20170129_5");
				i_step_for71=2;
				i_egaki_dankai=1;
				line_step[1].set(moyori_senbun);
				return;
			}
			//if(oc.kyori_senbun( p,moyori_senbun)>=d_decision_width){
				//System.out.println("");
				moyori_ten.set(get_moyori_ten(p));
				if(p.kyori(moyori_ten)<d_decision_width){
					line_step[1].setb(moyori_ten);
					i_step_for71=2;i_egaki_dankai=1;
					return;
				}
				//System.out.println("20170129_7");
				i_egaki_dankai=0;i_candidate_stage=0;
				return;
			//}
			//return;
		}



		if(i_step_for71==2){//i_step_for71==2であれば、以下でs_step[1]を入力折線を確定する
			moyori_ten.set(get_moyori_ten(p));

			//System.out.println("20170130_1");
			if(moyori_ten.kyori(line_step[1].geta())< 0.00000001 ){
				i_egaki_dankai=0;i_candidate_stage=0;
				return;
			}
			//else if(p.kyori(line_step[1].getb())< kus.d_haba()/10.0 ){
			//else if(p.kyori(line_step[1].getb())< d_decision_width/2.5 ){
			//else if(p.kyori(line_step[1].getb())< d_decision_width ){

			if((p.kyori(line_step[1].getb())< d_decision_width )&&
				(
				p.kyori(line_step[1].getb())<=p.kyori(moyori_ten)
				//moyori_ten.kyori(line_step[1].getb())<0.00000001
				)){
				Senbun add_sen =new Senbun(line_step[1].geta(),line_step[1].getb(),lineColor);
				addsenbun(add_sen);
				kiroku();
				i_egaki_dankai=0;i_candidate_stage=0;
				return;
			}

		//}


		//if(i_step_for_copy_4p==2){

			//moyori_ten.set(get_moyori_ten(p));
			if(p.kyori(moyori_ten)<d_decision_width){
				line_step[1].setb(moyori_ten);return;
			}



			moyori_senbun.set(get_moyori_senbun(p));

			Senbun moyori_step_senbun =new Senbun();moyori_step_senbun.set(get_moyori_step_senbun(p,1,i_egaki_dankai));
			if(oc.kyori_senbun( p,moyori_senbun)>=d_decision_width){//最寄の既存折線が遠い場合
				//moyori_senbun.set(get_moyori_step_senbun(p,1,i_egaki_dankai));


				//moyori_ten.set(get_moyori_ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}
				//moyori_ten.set(foldLines.mottomo_tikai_Ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}



				if(oc.kyori_senbun( p,moyori_step_senbun)<d_decision_width){//最寄のstep_senbunが近い場合

					//moyori_ten.set(get_moyori_ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}




					return;
				}
				//最寄のstep_senbunが遠い場合

					//moyori_ten.set(get_moyori_ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}
				i_egaki_dankai=0;i_candidate_stage=0;
				return;
			}

			if(oc.kyori_senbun( p,moyori_senbun)<d_decision_width){//最寄の既存折線が近い場合
				//moyori_ten.set(foldLines.mottomo_tikai_Ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}
				line_step[2].set(moyori_senbun);
				line_step[2].setcolor(6);
				//System.out.println("20170129_3");
				Ten kousa_ten =new Ten(); kousa_ten.set(oc.kouten_motome(line_step[1],line_step[2]));
				Senbun add_sen =new Senbun(kousa_ten,line_step[1].geta(),lineColor);
				if(add_sen.getnagasa()>0.00000001){//最寄の既存折線が有効の場合
					addsenbun(add_sen);
					kiroku();
					i_egaki_dankai=0;i_candidate_stage=0;
					return;
				}
				//最寄の既存折線が無効の場合
				moyori_ten.set(get_moyori_ten(p));if(p.kyori(moyori_ten)<d_decision_width){line_step[1].setb(moyori_ten);return;}
				//最寄のstep_senbunが近い場合
				if(oc.kyori_senbun( p,moyori_step_senbun)<d_decision_width){
					return;
				}
				//最寄のstep_senbunが遠い場合
				i_egaki_dankai=0;i_candidate_stage=0;
				return;

			}
			return;
		}





	}

//マウス操作(ドラッグしたとき)を行う関数
	public void mDragged_A_71(Ten p0) {	}

//マウス操作(ボタンを離したとき)を行う関数
	public void mReleased_A_71(Ten p0){

	}












*/


//7777777777777777777    i_mouse_modeA==7;角二等分線モード　

    //70 70 70 70 70 70 70 70 70 70 70 70 70 70    i_mouse_modeA==70　;線分延長モード
    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_70(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_05or70(p0);
    }//常にマウスの位置のみが候補点

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_70(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_05or70(p0);
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_70(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_05or70(p0);
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_70(origami_editor.graphic2d.point.Point p0) {
        mReleased_A_05or70(p0);
    }

//------


//88888888888888888888888    i_mouse_modeA==8　;内心モード。

    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_05or70(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_003(p0, lineColor);
    }//常にマウスの位置のみが候補点

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_05or70(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        i_candidate_stage = 0;

        if (i_drawing_stage == 0) {
            entyou_kouho_nbox.reset();
            i_drawing_stage = 1;

            line_step[1].set(p, p);
            line_step[1].setColor(LineColor.MAGENTA_5);//マゼンタ
            return;
        }

        if (i_drawing_stage >= 2) {

            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(p, p);
            line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);//マゼンタ
            return;
        }

    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_05or70(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        if (i_drawing_stage == 1) {
            line_step[i_drawing_stage].setB(p);
        }
        if (i_drawing_stage > 1) {
            line_step[i_drawing_stage].set(p, p);
        }
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_05or70(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_lineSegment.set(getClosestLineSegment(p));


        if (i_drawing_stage == 1) {

            line_step[1].setB(p);


            for (int i = 1; i <= foldLines.getTotal(); i++) {
                LineSegment.Intersection i_lineSegment_intersection_decision = OritaCalc.line_intersect_decide(foldLines.get(i), line_step[1], 0.0001, 0.0001);
                int i_jikkou = 0;

                if (i_lineSegment_intersection_decision == LineSegment.Intersection.INTERSECTS_1) {
                    i_jikkou = 1;
                }
                //if(i_lineSegment_intersection_decision== 27 ){ i_jikkou=1;}
                //if(i_lineSegment_intersection_decision== 28 ){ i_jikkou=1;}

                if (i_jikkou == 1) {
                    int_double i_d = new int_double(i, OritaCalc.distance(line_step[1].getA(), OritaCalc.findIntersection(foldLines.get(i), line_step[1])));
                    entyou_kouho_nbox.container_i_smallest_first(i_d);
                }


            }
            if ((entyou_kouho_nbox.getTotal() == 0) && (line_step[1].getLength() <= 0.000001)) {//延長する候補になる折線を選ぶために描いた線分s_step[1]が点状のときの処理
                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                    int_double i_d = new int_double(foldLines.closestLineSegmentSearch(p), 1.0);//entyou_kouho_nboxに1本の情報しか入らないのでdoubleの部分はどうでもよいので適当に1.0にした。
                    entyou_kouho_nbox.container_i_smallest_first(i_d);

                    line_step[1].setB(OritaCalc.lineSymmetry_point_find(closest_lineSegment.getA(), closest_lineSegment.getB(), p));

                    line_step[1].set(//line_step[1]を短くして、表示時に目立たない様にする。
                            OritaCalc.point_double(OritaCalc.midPoint(line_step[1].getA(), line_step[1].getB()), line_step[1].getA(), 0.00001 / line_step[1].getLength())
                            ,
                            OritaCalc.point_double(OritaCalc.midPoint(line_step[1].getA(), line_step[1].getB()), line_step[1].getB(), 0.00001 / line_step[1].getLength())
                    );

                }

            }

            System.out.println(" entyou_kouho_nbox.getsousuu() = " + entyou_kouho_nbox.getTotal());


            if (entyou_kouho_nbox.getTotal() == 0) {
                i_drawing_stage = 0;
                return;
            }
            if (entyou_kouho_nbox.getTotal() >= 0) {

                i_drawing_stage = 1 + entyou_kouho_nbox.getTotal();

                for (int i = 2; i <= i_drawing_stage; i++) {
                    line_step[i].set(foldLines.get(entyou_kouho_nbox.getInt(i - 1)));
                    line_step[i].setColor(LineColor.GREEN_6);//グリーン
                }
                return;
            }
            return;
        }


        if (i_drawing_stage >= 3) {
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {
                i_drawing_stage = 0;
                return;
            }

            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {


                //最初に選んだ延長候補線分群中に2番目に選んだ線分と等しいものがあるかどうかを判断する。
                int i_senbun_entyou_mode = 0;// i_senbun_entyou_mode=0なら最初に選んだ延長候補線分群中に2番目に選んだ線分と等しいものがない。1ならある。
                for (int i = 1; i <= entyou_kouho_nbox.getTotal(); i++) {
                    if (OritaCalc.line_intersect_decide(foldLines.get(entyou_kouho_nbox.getInt(i)), closest_lineSegment, 0.000001, 0.000001) == LineSegment.Intersection.PARALLEL_EQUAL_31) {//線分が同じならoc.senbun_kousa_hantei==31
                        i_senbun_entyou_mode = 1;
                    }
                }


                LineSegment add_sen = new LineSegment();
                //最初に選んだ延長候補線分群中に2番目に選んだ線分と等しいものがない場合
                if (i_senbun_entyou_mode == 0) {
                    int sousuu_old = foldLines.getTotal();//(1)
                    for (int i = 1; i <= entyou_kouho_nbox.getTotal(); i++) {
                        //最初に選んだ線分と2番目に選んだ線分が平行でない場合
                        if (OritaCalc.parallel_judgement(foldLines.get(entyou_kouho_nbox.getInt(i)), closest_lineSegment, 0.000001) == OritaCalc.ParallelJudgement.NOT_PARALLEL) { //２つの線分が平行かどうかを判定する関数。oc.heikou_hantei(Tyokusen t1,Tyokusen t2)//0=平行でない
                            //line_step[1]とs_step[2]の交点はoc.kouten_motome(Senbun s1,Senbun s2)で求める//２つの線分を直線とみなして交点を求める関数。線分としては交差しなくても、直線として交差している場合の交点を返す
                            origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
                            kousa_point.set(OritaCalc.findIntersection(foldLines.get(entyou_kouho_nbox.getInt(i)), closest_lineSegment));
                            //add_sen =new Senbun(kousa_ten,foldLines.get(entyou_kouho_nbox.get_int(i)).get_tikai_hasi(kousa_ten));
                            add_sen.setA(kousa_point);
                            add_sen.setB(foldLines.get(entyou_kouho_nbox.getInt(i)).getClosestEndpoint(kousa_point));


                            if (add_sen.getLength() > 0.00000001) {
                                if (app.i_mouse_modeA == MouseMode.LENGTHEN_CREASE_5) {
                                    add_sen.setColor(lineColor);
                                }
                                if (app.i_mouse_modeA == MouseMode.CREASE_LENGTHEN_70) {
                                    add_sen.setColor(foldLines.get(entyou_kouho_nbox.getInt(i)).getColor());
                                }

                                //addsenbun(add_sen);
                                foldLines.addLine(add_sen);//ori_sのsenbunの最後にs0の情報をを加えるだけ//(2)
                            }
                        }
                    }
                    foldLines.lineSegment_circle_intersection(sousuu_old, foldLines.getTotal(), 1, foldLines.numCircles());//(3)
                    foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, foldLines.getTotal());//(4)


                }

                //最初に選んだ延長候補線分群中に2番目に選んだ線分と等しいものがある場合
                if (i_senbun_entyou_mode == 1) {

                    int sousuu_old = foldLines.getTotal();//(1)
                    for (int i = 1; i <= entyou_kouho_nbox.getTotal(); i++) {
                        LineSegment moto_no_sen = new LineSegment();
                        moto_no_sen.set(foldLines.get(entyou_kouho_nbox.getInt(i)));
                        origami_editor.graphic2d.point.Point p_point = new origami_editor.graphic2d.point.Point();
                        p_point.set(OritaCalc.findIntersection(moto_no_sen, line_step[1]));

                        if (p_point.distance(moto_no_sen.getA()) < p_point.distance(moto_no_sen.getB())) {
                            moto_no_sen.a_b_swap();
                        }
                        add_sen.set(extendToIntersectionPoint_2(moto_no_sen));


                        if (add_sen.getLength() > 0.00000001) {
                            if (app.i_mouse_modeA == MouseMode.LENGTHEN_CREASE_5) {
                                add_sen.setColor(lineColor);
                            }
                            if (app.i_mouse_modeA == MouseMode.CREASE_LENGTHEN_70) {
                                add_sen.setColor(foldLines.get(entyou_kouho_nbox.getInt(i)).getColor());
                            }

                            foldLines.addLine(add_sen);//ori_sのsenbunの最後にs0の情報をを加えるだけ//(2)
                        }

                    }
                    foldLines.lineSegment_circle_intersection(sousuu_old, foldLines.getTotal(), 1, foldLines.numCircles());//(3)
                    foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, foldLines.getTotal());//(4)


                }


                record();


                i_drawing_stage = 0;
            }
        }


    }

    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_71(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 0) {
            i_dousa_mode = 0;
            mMoved_A_01(p0);
            return;
        }

        if (i_dousa_mode == 1) {
            mMoved_A_01(p0);
        }
        if (i_dousa_mode == 38) {
            mMoved_A_38(p0);
        }
    }

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_71(origami_editor.graphic2d.point.Point p0) {
        i_dousa_mode_henkou_kanousei = 0;

        p.set(camera.TV2object(p0));
        double hantei_kyori = 0.000001;

        if (p.distance(moyori_point_memo) <= d_decision_width) {
            i_drawing_stage = 0;
        }


        if (i_drawing_stage == 0) {


            //任意の点が与えられたとき、端点もしくは格子点で最も近い点を得る
            closest_point.set(getClosestPoint(p));
            moyori_point_memo.set(closest_point);

            if (p.distance(closest_point) > d_decision_width) {
                closest_point.set(p);
            }

            //moyori_tenを端点とする折線をNarabebakoに入れる
            SortingBox_int_double nbox = new SortingBox_int_double();
            for (int i = 1; i <= foldLines.getTotal(); i++) {
                if (foldLines.getColor(i).isFoldingLine()) {
                    if (closest_point.distance(foldLines.getA(i)) < hantei_kyori) {
                        nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getA(i), foldLines.getB(i))));
                    } else if (closest_point.distance(foldLines.getB(i)) < hantei_kyori) {
                        nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getB(i), foldLines.getA(i))));
                    }
                }
            }
            if (nbox.getTotal() % 2 == 0) {
                i_dousa_mode = 1;
                i_foldLine_additional = FoldLineAdditionalInputMode.POLY_LINE_0;
            }//moyori_tenを端点とする折線の数が偶数のときif{}内の処理をする
            if (nbox.getTotal() % 2 == 1) {
                i_dousa_mode = 38;
                i_dousa_mode_henkou_kanousei = 1;
            }//moyori_tenを端点とする折線の数が奇数のときif{}内の処理をする

        }

        if (i_dousa_mode == 1) {
            mPressed_A_01(p0);
        }
        if (i_dousa_mode == 38) {
            if (mPressed_A_38(p0) == 0) {
                if (i_drawing_stage == 0) {
                    mPressed_A_71(p0);
                }
            }
        }


    }

    //マウス操作(ドラッグしたとき)を行う関数20200
    public void mDragged_A_71(origami_editor.graphic2d.point.Point p0) {
        if ((i_dousa_mode == 38) && (i_dousa_mode_henkou_kanousei == 1)) {
            //if(i_dousa_mode==38){
            p.set(camera.TV2object(p0));
            moyori_point_memo.set(closest_point);
            if (p.distance(moyori_point_memo) > d_decision_width) {
                i_dousa_mode = 1;
                i_drawing_stage = 1;
                line_step[1].a_b_swap();
                line_step[1].setColor(lineColor);
                i_dousa_mode_henkou_kanousei = 0;
            }

        }

        if (i_dousa_mode == 1) {
            mDragged_A_01(p0);
        }
        if (i_dousa_mode == 38) {
            mDragged_A_38(p0);
        }
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_71(origami_editor.graphic2d.point.Point p0) {
        if (i_dousa_mode == 1) {
            mReleased_A_01(p0);
        }
        if (i_dousa_mode == 38) {
            mReleased_A_38(p0);
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_07(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage >= 0) && (i_drawing_stage <= 2)) {
            mMoved_A_29(p0);//近い既存点のみ表示
        }

    }

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_07(origami_editor.graphic2d.point.Point p0) {


        origami_editor.graphic2d.point.Point p = new origami_editor.graphic2d.point.Point();
        p.set(camera.TV2object(p0));

        if ((i_drawing_stage >= 0) && (i_drawing_stage <= 2)) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(lineColor);
                return;
            }
        }

        if (i_drawing_stage == 3) {
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
            }
        }

    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_07(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_07(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 4) {
            i_drawing_stage = 0;

            //三角形の内心を求める	public Ten oc.naisin(Ten ta,Ten tb,Ten tc)
            origami_editor.graphic2d.point.Point naisin = new origami_editor.graphic2d.point.Point();
            naisin.set(OritaCalc.center(line_step[1].getA(), line_step[2].getA(), line_step[3].getA()));


            LineSegment add_sen2 = new LineSegment(line_step[2].getA(), naisin);


            //add_sen2とs_step[4]の交点はoc.kouten_motome(Senbun s1,Senbun s2)で求める//２つの線分を直線とみなして交点を求める関数。線分としては交差しなくても、直線として交差している場合の交点を返す
            origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
            kousa_point.set(OritaCalc.findIntersection(add_sen2, line_step[4]));

            LineSegment add_sen = new LineSegment(kousa_point, line_step[2].getA(), lineColor);
            if (add_sen.getLength() > 0.00000001) {
                addLineSegment(add_sen);
                record();
            }


        }


    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_08(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

//------

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_08(origami_editor.graphic2d.point.Point p0) {


        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }


    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_08(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_08(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;

            //三角形の内心を求める	public Ten oc.naisin(Ten ta,Ten tb,Ten tc)
            origami_editor.graphic2d.point.Point naisin = new origami_editor.graphic2d.point.Point();
            naisin.set(OritaCalc.center(line_step[1].getA(), line_step[2].getA(), line_step[3].getA()));

            LineSegment add_sen1 = new LineSegment(line_step[1].getA(), naisin, lineColor);
            if (add_sen1.getLength() > 0.00000001) {
                addLineSegment(add_sen1);
            }
            LineSegment add_sen2 = new LineSegment(line_step[2].getA(), naisin, lineColor);
            if (add_sen2.getLength() > 0.00000001) {
                addLineSegment(add_sen2);
            }
            LineSegment add_sen3 = new LineSegment(line_step[3].getA(), naisin, lineColor);
            if (add_sen3.getLength() > 0.00000001) {
                addLineSegment(add_sen3);
            }
            record();
        }


    }

    //------
    public double get_L1() {
        return measured_length_1;
    }

//------

    public double get_L2() {
        return measured_length_2;
    }

    public double get_A1() {
        return measured_angle_1;
    }

    public double get_A2() {
        return measured_angle_2;
    }

    public double get_A3() {
        return measured_angle_3;
    }
//------

    //53 53 53 53 53 53 53 53 53    i_mouse_modeA==53　;長さ測定１モード。
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_53(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //Work when operating the mouse (when the button is pressed)
    public void mPressed_A_53(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_53(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_53(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            measured_length_1 = OritaCalc.distance(line_step[1].getA(), line_step[2].getA()) * (double) grid.divisionNumber() / 400.0;

            app.measured_length_1_display(measured_length_1);
            //kiroku();
        }


    }
//------

    //------
//54 54 54 54 54 54 54 54 54    i_mouse_modeA==54　;長さ測定2モード。
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_54(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_54(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_54(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_54(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            measured_length_2 = OritaCalc.distance(line_step[1].getA(), line_step[2].getA()) * (double) grid.divisionNumber() / 400.0;

            app.measured_length_2_display(measured_length_2);
            //kiroku();
        }


    }
//------


//999999999999999999    i_mouse_modeA==9　;垂線おろしモード

    //------
//55 55 55 55 55 55 55 55 55    i_mouse_modeA==55　;角度測定1モード。
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_55(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_55(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_55(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_55(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
            measured_angle_1 = OritaCalc.angle(line_step[2].getA(), line_step[3].getA(), line_step[2].getA(), line_step[1].getA());
            if (measured_angle_1 > 180.0) {
                measured_angle_1 = measured_angle_1 - 360.0;
            }

            app.measured_angle_1_display(measured_angle_1);
            //kiroku();
        }
    }
//------
//------
//40 40 40 40 40 40     i_mouse_modeA==40　;平行線入力モード

    //------
//56 56 56 56 56 56 56 56 56    i_mouse_modeA==56　;角度測定2モード。
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_56(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_56(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_56(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_56(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
            measured_angle_2 = OritaCalc.angle(line_step[2].getA(), line_step[3].getA(), line_step[2].getA(), line_step[1].getA());
            if (measured_angle_2 > 180.0) {
                measured_angle_2 = measured_angle_2 - 360.0;
            }
            app.measured_angle_2_display(measured_angle_2);
            //kiroku();
        }
    }

    //------
//57 57 57 57 57 57 57 57 57    i_mouse_modeA==57　;角度測定3モード。
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_57(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示


//10 10 10 10 10    i_mouse_modeA==10　;折り返しモード

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_57(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_57(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_57(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
            measured_angle_3 = OritaCalc.angle(line_step[2].getA(), line_step[3].getA(), line_step[2].getA(), line_step[1].getA());
            if (measured_angle_3 > 180.0) {
                measured_angle_3 = measured_angle_3 - 360.0;
            }
            app.measured_angle_3_display(measured_angle_3);
            //kiroku();
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_09(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 0) {
            mMoved_A_29(p0);//近い既存点のみ表示
        }

    }


//52 52 52 52 52    i_mouse_modeA==52　;連続折り返しモード ****************************************

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_09(origami_editor.graphic2d.point.Point p0) {


        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        if (i_drawing_stage == 0) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(lineColor);
                return;
            }
        }

        if (i_drawing_stage == 1) {
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);
                line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                return;
            }
            i_drawing_stage = 0;
        }

    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_09(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_09(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            //直線t上の点pの影の位置（点pと最も近い直線t上の位置）を求める。public Ten oc.kage_motome(Tyokusen t,Ten p){
            //oc.Senbun2Tyokusen(Senbun s)//線分を含む直線を得る

            LineSegment add_sen = new LineSegment(line_step[1].getA(), OritaCalc.findProjection(OritaCalc.lineSegmentToStraightLine(line_step[2]), line_step[1].getA()), lineColor);
            if (add_sen.getLength() > 0.00000001) {
                addLineSegment(add_sen);
                record();
            }


        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_40(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 0) {
            mMoved_A_29(p0);//近い既存点のみ表示
        }

    }

// ------------------------------------------------------------
    //２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){
    //Ten t_taisyou =new Ten(); t_taisyou.set(oc.sentaisyou_ten_motome(line_step[2].geta(),line_step[3].geta(),line_step[1].geta()));

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_40(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        if (i_drawing_stage == 0) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(lineColor);
                return;
            }
        }

        if (i_drawing_stage == 1) {
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                return;
            }
            return;
        }


        if (i_drawing_stage == 2) {
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                return;
            }
        }
    }

// ------------------------------------------------------------

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_40(origami_editor.graphic2d.point.Point p0) {
    }
// ------------------------------------------------------------

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_40(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
            //line_step[1]を点状から、line_step[2]に平行な線分にする。
            line_step[1].setB(new origami_editor.graphic2d.point.Point(line_step[1].getAX() + line_step[2].getBX() - line_step[2].getAX(), line_step[1].getAY() + line_step[2].getBY() - line_step[2].getAY()));


            //Ten kousa_ten =new Ten(); kousa_ten.set(oc.kouten_motome(line_step[1],line_step[3]));

            //Senbun add_sen =new Senbun(kousa_ten,line_step[1].geta(),lineColor);

            if (s_step_tuika_koutenmade(3, line_step[1], line_step[3], lineColor) > 0) {
                addLineSegment(line_step[4]);
                record();
                i_drawing_stage = 0;
            }
        }
    }
// ------------------------------------------------------------


//--------------------------------------------
//27 27 27 27 27 27 27 27  i_mouse_modeA==27線分分割	入力 27 27 27 27 27 27 27 27
    //動作概要　
    //i_mouse_modeA==1と線分分割以外は同じ　
    //

    //------
    //i_egaki_dankaiがi_e_dのときに、線分s_oをTen aはそのままで、Ten b側をs_kの交点までのばした一時折線s_step[i_e_d+1](色はicolo)を追加。成功した場合は1、なんらかの不都合で追加できなかった場合は-500を返す。
    public int s_step_tuika_koutenmade(int i_e_d, LineSegment s_o, LineSegment s_k, LineColor icolo) {

        origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();

        if (OritaCalc.parallel_judgement(s_o, s_k, 0.0000001) == OritaCalc.ParallelJudgement.PARALLEL_NOT_EQUAL) {//0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する
            return -500;
        }

        if (OritaCalc.parallel_judgement(s_o, s_k, 0.0000001) == OritaCalc.ParallelJudgement.PARALLEL_EQUAL) {//0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する
            kousa_point.set(s_k.getA());
            if (OritaCalc.distance(s_o.getA(), s_k.getA()) > OritaCalc.distance(s_o.getA(), s_k.getB())) {
                kousa_point.set(s_k.getB());
            }


        }

        if (OritaCalc.parallel_judgement(s_o, s_k, 0.0000001) == OritaCalc.ParallelJudgement.NOT_PARALLEL) {//0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する
            kousa_point.set(OritaCalc.findIntersection(s_o, s_k));
        }


        LineSegment add_sen = new LineSegment(kousa_point, s_o.getA(), icolo);

        if (add_sen.getLength() > 0.00000001) {
            line_step[i_e_d + 1].set(add_sen);
            return 1;
        }
        return -500;
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_10(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_10(origami_editor.graphic2d.point.Point p0) {

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_10(origami_editor.graphic2d.point.Point p0) {
    }

//--------------------------------------------
//29 29 29 29 29 29 29 29  i_mouse_modeA==29正多角形入力	入力 29 29 29 29 29 29 29 29
    //動作概要　
    //i_mouse_modeA==1と線分分割以外は同じ　
    //

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_10(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;

            //２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){
            origami_editor.graphic2d.point.Point t_taisyou = new origami_editor.graphic2d.point.Point();
            t_taisyou.set(OritaCalc.lineSymmetry_point_find(line_step[2].getA(), line_step[3].getA(), line_step[1].getA()));

            LineSegment add_sen = new LineSegment(line_step[2].getA(), t_taisyou);

            add_sen.set(extendToIntersectionPoint(add_sen));
            add_sen.setColor(lineColor);
            if (add_sen.getLength() > 0.00000001) {
                addLineSegment(add_sen);
                record();
            }
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_52(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_52(origami_editor.graphic2d.point.Point p0) {
        System.out.println("i_egaki_dankai=" + i_drawing_stage);

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));

        i_drawing_stage = i_drawing_stage + 1;
        if (p.distance(closest_point) < d_decision_width) {
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(lineColor);
        } else {
            line_step[i_drawing_stage].set(p, p);
            line_step[i_drawing_stage].setColor(lineColor);
        }

        System.out.println("i_egaki_dankai=" + i_drawing_stage);
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_52(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_52(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;

            LineSegment add_lineSegment = new LineSegment();
            continuous_folding_new(line_step[1].getA(), line_step[2].getA());
            for (int i = 1; i <= i_drawing_stage; i++) {
                if (line_step[i].getLength() > 0.00000001) {
                    add_lineSegment.set(line_step[i].getA(), line_step[i].getB());//要注意　s_stepは表示上の都合でアクティヴが0以外に設定されているのでadd_senbunにうつしかえてる20170507
                    add_lineSegment.setColor(lineColor);
                    addLineSegment(add_lineSegment);
                }
            }
            record();

            i_drawing_stage = 0;
        }
    }

    // ------------------------------------------------------------
    public void continuous_folding_new(origami_editor.graphic2d.point.Point a, origami_editor.graphic2d.point.Point b) {//An improved version of continuous folding.
        app.repaint();

        //ベクトルab(=s0)を点aからb方向に、最初に他の折線(直線に含まれる線分は無視。)と交差するところまで延長する

        //与えられたベクトルabを延長して、それと重ならない折線との、最も近い交点までs_stepとする。
        //補助活線は無視する
        //与えられたベクトルabを延長して、それと重ならない折線との、最も近い交点までs_stepとする


        //「再帰関数における、種の発芽」交点がない場合「種」が成長せずリターン。

        e_s_dougubako.lengthenUntilIntersectionCalculateDisregardIncludedLineSegment_new(a, b);//一番近い交差点を見つけて各種情報を記録
        if (e_s_dougubako.getLengthenUntilIntersectionFlg_new(a, b) == StraightLine.Intersection.NONE_0) {
            return;
        }

        //「再帰関数における、種の成長」交点が見つかった場合、交点まで伸びる線分をs_step[i_egaki_dankai]に追加
        //if(e_s_dougubako.get_kousaten_made_nobasi_orisen_fukumu_flg(a,b)==3){return;}
        i_drawing_stage = i_drawing_stage + 1;
        if (i_drawing_stage > 100) {
            return;
        }//念のためにs_stepの上限を100に設定した

        line_step[i_drawing_stage].set(e_s_dougubako.getLengthenUntilIntersectionLineSegment_new());//要注意　es1でうっかりs_stepにset.(senbun)やるとアクティヴでないので表示が小さくなる20170507
        line_step[i_drawing_stage].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

        System.out.println("20201129 saiki repaint ");

        //「再帰関数における、種の生成」求めた最も近い交点から次のベクトル（＝次の再帰関数に渡す「種」）を発生する。最も近い交点が折線とＸ字型に交差している点か頂点かで、種のでき方が異なる。

        //最も近い交点が折線とＸ字型の場合無条件に種を生成し、散布。
        if (e_s_dougubako.getLengthenUntilIntersectionFlg_new(a, b) == StraightLine.Intersection.INTERSECT_X_1) {
            LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
            kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment_new());

            origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
            new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint_new());//Ten new_aは最も近い交点
            origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
            new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

            continuous_folding_new(new_a, new_b);//種の散布
            return;
        }

        //最も近い交点が頂点（折線端末）の場合、頂点に集まる折線の数で条件分けして、種を生成し散布、
        if ((e_s_dougubako.getLengthenUntilIntersectionFlg_new(a, b) == StraightLine.Intersection.INTERSECT_T_A_21)
                || (e_s_dougubako.getLengthenUntilIntersectionFlg_new(a, b) == StraightLine.Intersection.INTERSECT_T_B_22)) {//System.out.println("20201129 21 or 22");

            StraightLine tyoku1 = new StraightLine(a, b);
            StraightLine.Intersection i_kousa_flg;

            SortingBox_int_double t_m_s_nbox = new SortingBox_int_double();

            t_m_s_nbox.set(foldLines.get_SortingBox_of_vertex_b_surrounding_foldLine(e_s_dougubako.getLengthenUntilIntersectionLineSegment_new().getA(), e_s_dougubako.getLengthenUntilIntersectionLineSegment_new().getB()));

            //System.out.println("20201129 t_m_s_nbox.getsousuu() = "+ t_m_s_nbox.getsousuu());


            if (t_m_s_nbox.getTotal() == 2) {

                //i_kousa_flg=
                //0=この直線は与えられた線分と交差しない、
                //1=X型で交差する、
                //21=線分のa点でT型で交差する、
                //22=線分のb点でT型で交差する、
                //3=線分は直線に含まれる。
                i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(1)));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    return;
                }

                i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(2)));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    return;
                }

                StraightLine tyoku2 = new StraightLine(foldLines.get(t_m_s_nbox.getInt(1)));
                i_kousa_flg = tyoku2.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(2)));
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
                    kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment_new());

                    origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
                    new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint_new());//Ten new_aは最も近い交点
                    origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
                    new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

                    continuous_folding_new(new_a, new_b);//種の散布
                    return;
                }
                return;
            }


            if (t_m_s_nbox.getTotal() == 3) {

                i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(1)));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    StraightLine tyoku2 = new StraightLine(foldLines.get(t_m_s_nbox.getInt(2)));
                    i_kousa_flg = tyoku2.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(3)));
                    if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                        LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
                        kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment_new());

                        origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
                        new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint_new());//Ten new_aは最も近い交点
                        origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
                        new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

                        continuous_folding_new(new_a, new_b);//種の散布
                        return;
                    }
                }
                //------------------------------------------------
                i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(2)));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    StraightLine tyoku2 = new StraightLine(foldLines.get(t_m_s_nbox.getInt(3)));
                    i_kousa_flg = tyoku2.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(1)));
                    if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                        LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
                        kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment_new());

                        origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
                        new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint_new());//Ten new_aは最も近い交点
                        origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
                        new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

                        continuous_folding_new(new_a, new_b);//種の散布
                        return;
                    }
                }
                //------------------------------------------------
                i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(3)));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。
                if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                    StraightLine tyoku2 = new StraightLine(foldLines.get(t_m_s_nbox.getInt(1)));
                    i_kousa_flg = tyoku2.lineSegment_intersect_reverse_detail(foldLines.get(t_m_s_nbox.getInt(2)));
                    if (i_kousa_flg == StraightLine.Intersection.INCLUDED_3) {
                        LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
                        kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment_new());

                        origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
                        new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint_new());//Ten new_aは最も近い交点
                        origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
                        new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

                        continuous_folding_new(new_a, new_b);//種の散布
                    }
                }


            }
        }
    }

    public void continuous_folding(origami_editor.graphic2d.point.Point a, origami_editor.graphic2d.point.Point b) {

        //与えられたベクトルabを延長して、それと重ならない折線との、最も近い交点までs_stepとする
        if (e_s_dougubako.getLengthenUntilIntersectionFlg(a, b) == StraightLine.Intersection.NONE_0) {
            return;
        }
        //if(e_s_dougubako.get_kousaten_made_nobasi_orisen_fukumu_flg(a,b)==3){return;}

        i_drawing_stage = i_drawing_stage + 1;
        if (i_drawing_stage > 100) {
            return;
        }//念のためにs_stepの上限を100に設定した

        line_step[i_drawing_stage].set(e_s_dougubako.getLengthenUntilIntersectionLineSegment(a, b));//要注意　es1でうっかりs_stepにset.(senbun)やるとアクティヴでないので表示が小さくなる20170507
        line_step[i_drawing_stage].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

        //求めた交点で、次のベクトルを発生する。

        if (e_s_dougubako.getLengthenUntilIntersectionFlg(a, b) == StraightLine.Intersection.INTERSECT_X_1) {
            LineSegment kousaten_made_nobasi_saisyono_lineSegment = new LineSegment();
            kousaten_made_nobasi_saisyono_lineSegment.set(e_s_dougubako.getLengthenUntilIntersectionFirstLineSegment(a, b));

            origami_editor.graphic2d.point.Point new_a = new origami_editor.graphic2d.point.Point();
            new_a.set(e_s_dougubako.getLengthenUntilIntersectionPoint(a, b));
            origami_editor.graphic2d.point.Point new_b = new origami_editor.graphic2d.point.Point();
            new_b.set(OritaCalc.lineSymmetry_point_find(kousaten_made_nobasi_saisyono_lineSegment.getA(), kousaten_made_nobasi_saisyono_lineSegment.getB(), a));//２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){

            continuous_folding(new_a, new_b);
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_27(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00a(p0, lineColor);//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。
    }

    //マウス操作(i_mouse_modeA==27線分入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_27(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            line_step[1].set(p, closest_point);
            line_step[1].setColor(lineColor);
            return;
        }
        line_step[1].set(p, p);
        line_step[1].setColor(lineColor);
    }


// 19 19 19 19 19 19 19 19 19 select 選択

    //マウス操作(i_mouse_modeA==27線分入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_27(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        if (i_kou_mitudo_nyuuryoku) {
            i_candidate_stage = 0;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_candidate_stage = 1;
                line_candidate[1].set(closest_point, closest_point);
                line_candidate[1].setColor(lineColor);
                line_step[1].setA(line_candidate[1].getA());
            }
        }


    }

    //マウス操作(i_mouse_modeA==27線分入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_27(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));

        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));

        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            for (int i = 0; i <= foldLineDividingNumber - 1; i++) {
                double ax = ((double) (foldLineDividingNumber - i) * line_step[1].getAX() + (double) i * line_step[1].getBX()) / ((double) foldLineDividingNumber);
                double ay = ((double) (foldLineDividingNumber - i) * line_step[1].getAY() + (double) i * line_step[1].getBY()) / ((double) foldLineDividingNumber);
                double bx = ((double) (foldLineDividingNumber - i - 1) * line_step[1].getAX() + (double) (i + 1) * line_step[1].getBX()) / ((double) foldLineDividingNumber);
                double by = ((double) (foldLineDividingNumber - i - 1) * line_step[1].getAY() + (double) (i + 1) * line_step[1].getBY()) / ((double) foldLineDividingNumber);
                LineSegment s_ad = new LineSegment(ax, ay, bx, by);
                s_ad.setColor(lineColor);
                addLineSegment(s_ad);
            }
            record();
        }

    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_29(origami_editor.graphic2d.point.Point p0) {
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
            i_candidate_stage = 0;
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_candidate_stage = 1;
                line_candidate[1].set(closest_point, closest_point);
                line_candidate[1].setColor(lineColor);
            }
        }
    }

    //マウス操作(i_mouse_modeA==29正多角形入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_29(origami_editor.graphic2d.point.Point p0) {
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第1段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
            }
            return;
        }

        if (i_drawing_stage == 1) {    //第2段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
                line_step[1].setB(line_step[2].getB());
            }
            if (line_step[1].getLength() < 0.00000001) {
                i_drawing_stage = 0;
            }
        }
    }

    //マウス操作(i_mouse_modeA==29正多角形入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_29(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(i_mouse_modeA==29正多角形入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_29(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            LineSegment s_tane = new LineSegment();
            LineSegment s_deki = new LineSegment();


            s_tane.set(line_step[1]);
            s_tane.setColor(lineColor);
            addLineSegment(s_tane);
            for (int i = 2; i <= numPolygonCorners; i++) {
                s_deki.set(OritaCalc.lineSegment_rotate(s_tane, (double) (numPolygonCorners - 2) * 180.0 / (double) numPolygonCorners));
                s_tane.set(s_deki.getB(), s_deki.getA());
                s_tane.setColor(lineColor);
                addLineSegment(s_tane);

            }
            foldLines.unselect_all();
            record();
        }
    }

    //37 37 37 37 37 37 37 37 37 37 37;角度規格化
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_37(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_29(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==37　でボタンを押したとき)時の作業-------//System.out.println("A");---------------------------------------------
    public void mPressed_A_37(origami_editor.graphic2d.point.Point p0) {
        line_step[1].setActive(LineSegment.ActiveState.ACTIVE_B_2);
        i_drawing_stage = 1;

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(lineColor);

        line_step[2].set(line_step[1]);//ここではs_step[2]は表示されない、計算用の線分
    }

    //マウス操作(i_mouse_modeA==37　でドラッグしたとき)を行う関数--------------//System.out.println("A");--------------------------------------
    public void mDragged_A_37(origami_editor.graphic2d.point.Point p0) {
        origami_editor.graphic2d.point.Point syuusei_point = new origami_editor.graphic2d.point.Point(syuusei_ten_A_37(p0));
        line_step[1].setA(syuusei_point);

        if (i_kou_mitudo_nyuuryoku) {
            i_candidate_stage = 1;
            line_candidate[1].set(kouho_ten_A_37(syuusei_point), kouho_ten_A_37(syuusei_point));
            line_candidate[1].setColor(lineColor);
            line_step[1].setA(kouho_ten_A_37(syuusei_point));
        }

    }

    //マウス操作(i_mouse_modeA==37　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_37(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            origami_editor.graphic2d.point.Point syuusei_point = new origami_editor.graphic2d.point.Point(syuusei_ten_A_37(p0));
            line_step[1].setA(kouho_ten_A_37(syuusei_point));
            if (line_step[1].getLength() > 0.00000001) {
                addLineSegment(line_step[1]);
                record();
            }
        }
    }


//------------------------------------------------------------
// 19 19 19 19 19 19 19 19 19 select 選択

    public origami_editor.graphic2d.point.Point syuusei_ten_A_37(origami_editor.graphic2d.point.Point p0) {

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        origami_editor.graphic2d.point.Point syuusei_point = new origami_editor.graphic2d.point.Point();
        double d_rad = 0.0;
        line_step[2].setA(p);

        if (id_angle_system != 0) {
            d_angle_system = 180.0 / (double) id_angle_system;
            d_rad = (Math.PI / 180) * d_angle_system * (int) Math.round(OritaCalc.angle(line_step[2]) / d_angle_system);
        } else {
            double[] jk = new double[7];
            jk[0] = OritaCalc.angle(line_step[2]);//マウスで入力した線分がX軸となす角度
            jk[1] = d_restricted_angle_1 - 180.0;
            jk[2] = d_restricted_angle_2 - 180.0;
            jk[3] = d_restricted_angle_3 - 180.0;
            jk[4] = 360.0 - d_restricted_angle_1 - 180.0;
            jk[5] = 360.0 - d_restricted_angle_2 - 180.0;
            jk[6] = 360.0 - d_restricted_angle_3 - 180.0;

            double d_kakudo_sa_min = 1000.0;
            for (int i = 1; i <= 6; i++) {
                if (Math.min(OritaCalc.angle_between_0_360(jk[i] - jk[0]), OritaCalc.angle_between_0_360(jk[0] - jk[i])) < d_kakudo_sa_min) {
                    d_kakudo_sa_min = Math.min(OritaCalc.angle_between_0_360(jk[i] - jk[0]), OritaCalc.angle_between_0_360(jk[0] - jk[i]));
                    d_rad = (Math.PI / 180) * jk[i];
                }
            }
        }

        syuusei_point.set(OritaCalc.findProjection(line_step[2].getB(), new origami_editor.graphic2d.point.Point(line_step[2].getBX() + Math.cos(d_rad), line_step[2].getBY() + Math.sin(d_rad)), p));
        return syuusei_point;
    }

    // ---
    public origami_editor.graphic2d.point.Point kouho_ten_A_37(origami_editor.graphic2d.point.Point syuusei_point) {
        closest_point.set(getClosestPoint(syuusei_point));
        double zure_kakudo = OritaCalc.angle(line_step[2].getB(), syuusei_point, line_step[2].getB(), closest_point);
        int zure_flg = 0;
        if ((0.00001 < zure_kakudo) && (zure_kakudo <= 359.99999)) {
            zure_flg = 1;
        }
        if ((zure_flg == 0) && (syuusei_point.distance(closest_point) <= d_decision_width)) {//最寄点が角度系にのっていて、修正点とも近い場合
            return closest_point;
        }
        return syuusei_point;
    }

    //------------------------------------------------------------
    public void mPressed_A_box_select(origami_editor.graphic2d.point.Point p0) {
        p19_1.set(p0);

        i_drawing_stage = 0;

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        line_step[1].set(p, p);
        line_step[1].setColor(LineColor.MAGENTA_5);
        line_step[2].set(p, p);
        line_step[2].setColor(LineColor.MAGENTA_5);
        line_step[3].set(p, p);
        line_step[3].setColor(LineColor.MAGENTA_5);
        line_step[4].set(p, p);
        line_step[4].setColor(LineColor.MAGENTA_5);

    }

    public void mDragged_A_box_select(origami_editor.graphic2d.point.Point p0) {
        p19_2.set(p19_1.getX(), p0.getY());
        p19_4.set(p0.getX(), p19_1.getY());

        p19_a.set(camera.TV2object(p19_1));
        p19_b.set(camera.TV2object(p19_2));
        p19_c.set(camera.TV2object(p0));
        p19_d.set(camera.TV2object(p19_4));

        line_step[1].set(p19_a, p19_b);
        line_step[2].set(p19_b, p19_c);
        line_step[3].set(p19_c, p19_d);
        line_step[4].set(p19_d, p19_a);

        i_drawing_stage = 4;//line_step[4]まで描画するために、この行が必要
    }

    //マウス操作(i_mouse_modeA==19  select　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_19(origami_editor.graphic2d.point.Point p0) {
        System.out.println("19  select_");
        System.out.println("i_egaki_dankai=" + i_drawing_stage);

        if (i_drawing_stage == 0) {//i_select_modeを決める
            p.set(camera.TV2object(p0));
        }

        if (i_select_mode == 0) {
            mPressed_A_box_select(p0);
        } else if (i_select_mode == 1) {
            mPressed_A_21(p0);//move
        } else if (i_select_mode == 2) {
            mPressed_A_31(p0);//move 2p2p
        } else if (i_select_mode == 3) {
            mPressed_A_22(p0);//copy
        } else if (i_select_mode == 4) {
            mPressed_A_32(p0);//copy 2p2p
        } else if (i_select_mode == 5) {
            mPressed_A_12(p0);//鏡映
        }
    }

//20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20

    //マウス操作(i_mouse_modeA==19 select　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_19(origami_editor.graphic2d.point.Point p0) {
        //mDragged_A_box_select( p0);
        if (i_select_mode == 0) {
            mDragged_A_box_select(p0);
        } else if (i_select_mode == 1) {
            mDragged_A_21(p0);//move
        } else if (i_select_mode == 2) {
            mDragged_A_31(p0);//move 2p2p
        } else if (i_select_mode == 3) {
            mDragged_A_22(p0);//copy
        } else if (i_select_mode == 4) {
            mDragged_A_32(p0);//copy 2p2p
        } else if (i_select_mode == 5) {
            mDragged_A_12(p0);//鏡映
        }


    }

    //マウス操作(i_mouse_modeA==19 select　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_19(origami_editor.graphic2d.point.Point p0) {
        if (i_select_mode == 0) {
            mReleased_A_box_select(p0);
        } else if (i_select_mode == 1) {
            mReleased_A_21(p0);//move
        } else if (i_select_mode == 2) {
            mReleased_A_31(p0);//move 2p2p
        } else if (i_select_mode == 3) {
            mReleased_A_22(p0);//copy
        } else if (i_select_mode == 4) {
            mReleased_A_32(p0);//copy 2p2p
        } else if (i_select_mode == 5) {
            mReleased_A_12(p0);//鏡映
        }
    }

    public void mReleased_A_box_select(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;

        select(p19_1, p0);
        if (p19_1.distance(p0) <= 0.000001) {
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                foldLines.select(foldLines.closestLineSegmentSearch(p));
            }
        }

    }

    //マウス操作(i_mouse_modeA==19  select　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_20(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==19 select　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_20(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==20 select　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_20(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;
        unselect(p19_1, p0);

        if (p19_1.distance(p0) <= 0.000001) {
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                foldLines.unselect(foldLines.closestLineSegmentSearch(p));
            }
        }


    }

    public int getDrawingStage() {
        return i_drawing_stage;
    }

    public void setDrawingStage(int i) {
        i_drawing_stage = i;
    }

//61 61 61 61 61 61 61 61 61 61 61 61 i_mouse_modeA==61//長方形内選択（paintの選択に似せた選択機能）に使う
    //動作概要　
    //マウスボタン押されたとき　
    //用紙1/1分割時 		折線の端点のみが基準点。格子点が基準点になることはない。
    //用紙1/2から1/512分割時	折線の端点と用紙枠内（-200.0,-200.0 _ 200.0,200.0)）の格子点とが基準点
    //入力点Pが基準点から格子幅kus.d_haba()の1/4より遠いときは折線集合への入力なし
    //線分が長さがなく1点状のときは折線集合への入力なし

    //---------------------
    public void select_all() {
        foldLines.select_all();
    }

    public void unselect_all() {
        foldLines.unselect_all();
    }

    public void select(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();


        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();


        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());


        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));


        foldLines.select(p_a, p_b, p_c, p_d);
    }

    //--------------------
    public void unselect(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        foldLines.unselect(p_a, p_b, p_c, p_d);
    }


//22222222222222222222222222222222222222222222222222222222222222 展開図移動


    //public void mPressed_A_02(Ten p0) {	}//マウス操作(i_mouse_modeA==2　展開図移動でボタンを押したとき)時の作業
    //public void mDragged_A_02(Ten p0) {	}//マウス操作(i_mouse_modeA==2　展開図移動でドラッグしたとき)を行う関数
    //public void mReleased_A_02(Ten p0){	}//マウス操作(i_mouse_modeA==2　展開図移動でボタンを離したとき)を行う関数

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_61(origami_editor.graphic2d.point.Point p0) {
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);

            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            //line_candidate[1].setcolor(lineColor);
            line_candidate[1].setColor(LineColor.GREEN_6);
        }
    }

    //マウス操作(i_mouse_modeA==61　長方形内選択でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_61(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        origami_editor.graphic2d.point.Point p_new = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_ob1 = new origami_editor.graphic2d.point.Point();
        p_ob1.set(camera.TV2object(operationFrame_p1));
        origami_editor.graphic2d.point.Point p_ob2 = new origami_editor.graphic2d.point.Point();
        p_ob2.set(camera.TV2object(operationFrame_p2));
        origami_editor.graphic2d.point.Point p_ob3 = new origami_editor.graphic2d.point.Point();
        p_ob3.set(camera.TV2object(operationFrame_p3));
        origami_editor.graphic2d.point.Point p_ob4 = new origami_editor.graphic2d.point.Point();
        p_ob4.set(camera.TV2object(operationFrame_p4));

        double kyori_min = 100000.0;

        operationFrameMode = OperationFrameMode.NONE_0;
        if (i_drawing_stage == 0) {
            operationFrameMode = OperationFrameMode.CREATE_1;
        }
        if (i_drawing_stage == 4) {
            if (operationFrameBox.inside(p0) == Polygon.Intersection.OUTSIDE) {
                operationFrameMode = OperationFrameMode.CREATE_1;
            } else {
                operationFrameMode = OperationFrameMode.MOVE_BOX_4;
            }


            kyori_min = OritaCalc.min(OritaCalc.distance_lineSegment(p, p_ob1, p_ob2), OritaCalc.distance_lineSegment(p, p_ob2, p_ob3), OritaCalc.distance_lineSegment(p, p_ob3, p_ob4), OritaCalc.distance_lineSegment(p, p_ob4, p_ob1));
            if (kyori_min < d_decision_width) {
                operationFrameMode = OperationFrameMode.MOVE_SIDES_3;
            }


            if (p.distance(p_ob1) < d_decision_width) {
                p_new.set(operationFrame_p1);
                operationFrame_p1.set(operationFrame_p3);
                operationFrame_p3.set(p_new);
                operationFrameMode = OperationFrameMode.MOVE_POINTS_2;
            }
            if (p.distance(p_ob2) < d_decision_width) {
                p_new.set(operationFrame_p2);
                operationFrame_p2.set(operationFrame_p1);
                operationFrame_p1.set(operationFrame_p4);
                operationFrame_p4.set(operationFrame_p3);
                operationFrame_p3.set(p_new);
                operationFrameMode = OperationFrameMode.MOVE_POINTS_2;
            }
            if (p.distance(p_ob3) < d_decision_width) {
                p_new.set(operationFrame_p3);
                operationFrame_p1.set(operationFrame_p1);
                operationFrame_p3.set(p_new);
                operationFrameMode = OperationFrameMode.MOVE_POINTS_2;
            }
            if (p.distance(p_ob4) < d_decision_width) {
                p_new.set(operationFrame_p4);
                operationFrame_p4.set(operationFrame_p1);
                operationFrame_p1.set(operationFrame_p2);
                operationFrame_p2.set(operationFrame_p3);
                operationFrame_p3.set(p_new);
                operationFrameMode = OperationFrameMode.MOVE_POINTS_2;
            }

        }


        if (operationFrameMode == OperationFrameMode.MOVE_SIDES_3) {
            while (OritaCalc.distance_lineSegment(p, p_ob1, p_ob2) != kyori_min) {
                p_new.set(operationFrame_p1);
                operationFrame_p1.set(operationFrame_p2);
                operationFrame_p2.set(operationFrame_p3);
                operationFrame_p3.set(operationFrame_p4);
                operationFrame_p4.set(p_new);
                p_new.set(p_ob1);
                p_ob1.set(p_ob2);
                p_ob2.set(p_ob3);
                p_ob3.set(p_ob4);
                p_ob4.set(p_new);
            }

        }

        if (operationFrameMode == OperationFrameMode.CREATE_1) {
            i_drawing_stage = 4;

            p_new.set(p);

            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                p_new.set(closest_point);

            }

            operationFrame_p1.set(camera.object2TV(p_new));
            operationFrame_p2.set(camera.object2TV(p_new));
            operationFrame_p3.set(camera.object2TV(p_new));
            operationFrame_p4.set(camera.object2TV(p_new));
        }
    }

    //マウス操作(i_mouse_modeA==61　長方形内選択でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_61(origami_editor.graphic2d.point.Point p0) {

        p.set(camera.TV2object(p0));
        if (operationFrameMode == OperationFrameMode.MOVE_POINTS_2) {
            operationFrameMode = OperationFrameMode.CREATE_1;
        }

        origami_editor.graphic2d.point.Point p_new = new origami_editor.graphic2d.point.Point();

        if (!i_kou_mitudo_nyuuryoku) {
            p_new.set(p);
        }

        if (i_kou_mitudo_nyuuryoku) {
            closest_point.set(getClosestPoint(p));
            i_candidate_stage = 1;
            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }
            line_candidate[1].setColor(LineColor.GREEN_6);

            p_new.set(line_candidate[1].getA());
        }


        if (operationFrameMode == OperationFrameMode.MOVE_SIDES_3) {
            if (
                    (operationFrame_p1.getX() - operationFrame_p2.getX()) * (operationFrame_p1.getX() - operationFrame_p2.getX())
                            <
                            (operationFrame_p1.getY() - operationFrame_p2.getY()) * (operationFrame_p1.getY() - operationFrame_p2.getY())
            ) {
                operationFrame_p1.setX(camera.object2TV(p_new).getX());
                operationFrame_p2.setX(camera.object2TV(p_new).getX());
            }

            if (
                    (operationFrame_p1.getX() - operationFrame_p2.getX()) * (operationFrame_p1.getX() - operationFrame_p2.getX())
                            >
                            (operationFrame_p1.getY() - operationFrame_p2.getY()) * (operationFrame_p1.getY() - operationFrame_p2.getY())
            ) {
                operationFrame_p1.setY(camera.object2TV(p_new).getY());
                operationFrame_p2.setY(camera.object2TV(p_new).getY());
            }

        }


        if (operationFrameMode == OperationFrameMode.CREATE_1) {
            operationFrame_p3.set(camera.object2TV(p_new));
            operationFrame_p2.set(operationFrame_p1.getX(), operationFrame_p3.getY());
            operationFrame_p4.set(operationFrame_p3.getX(), operationFrame_p1.getY());
        }
    }

//--------------------

    //マウス操作(i_mouse_modeA==61 長方形内選択　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_61(origami_editor.graphic2d.point.Point p0) {

        p.set(camera.TV2object(p0));

        origami_editor.graphic2d.point.Point p_new = new origami_editor.graphic2d.point.Point();
        p_new.set(p);

        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            p_new.set(closest_point);/*line_step[1].seta(moyori_ten);*/
        }

        if (operationFrameMode == OperationFrameMode.MOVE_SIDES_3) {
            if (
                    (operationFrame_p1.getX() - operationFrame_p2.getX()) * (operationFrame_p1.getX() - operationFrame_p2.getX())
                            <
                            (operationFrame_p1.getY() - operationFrame_p2.getY()) * (operationFrame_p1.getY() - operationFrame_p2.getY())
            ) {
                operationFrame_p1.setX(camera.object2TV(p_new).getX());
                operationFrame_p2.setX(camera.object2TV(p_new).getX());
            }

            if (
                    (operationFrame_p1.getX() - operationFrame_p2.getX()) * (operationFrame_p1.getX() - operationFrame_p2.getX())
                            >
                            (operationFrame_p1.getY() - operationFrame_p2.getY()) * (operationFrame_p1.getY() - operationFrame_p2.getY())
            ) {
                operationFrame_p1.setY(camera.object2TV(p_new).getY());
                operationFrame_p2.setY(camera.object2TV(p_new).getY());
            }

        }

        if (operationFrameMode == OperationFrameMode.CREATE_1) {
            operationFrame_p3.set(camera.object2TV(p_new));
            operationFrame_p2.set(operationFrame_p1.getX(), operationFrame_p3.getY());
            operationFrame_p4.set(operationFrame_p3.getX(), operationFrame_p1.getY());
        }

        operationFrameBox.set(1, operationFrame_p1);
        operationFrameBox.set(2, operationFrame_p2);
        operationFrameBox.set(3, operationFrame_p3);
        operationFrameBox.set(4, operationFrame_p4);

        if (operationFrameBox.calculateArea() * operationFrameBox.calculateArea() < 1.0) {
            i_drawing_stage = 0;
        }
    }
//--------------------

    //3 3 3 3 3 33333333333333333333333333333333333333333333333333333333
    //マウス操作(i_mouse_modeA==3,23 "線分削除" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_03(origami_editor.graphic2d.point.Point p0) {
        //System.out.println("(1)zzzzz foldLines.check4_size() = "+foldLines.check4_size());
        if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
            mPressed_A_box_select(p0);
        }//折線の削除
        if (i_foldLine_additional == FoldLineAdditionalInputMode.BLACK_LINE_2) {
            mPressed_A_box_select(p0);
        }//黒の折線
        if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LIVE_LINE_3) {
            mPressed_A_box_select(p0);
        }//補助活線

        if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
            mPressed_A_box_select(p0);
        }//補助絵線

        if (i_foldLine_additional == FoldLineAdditionalInputMode.BOTH_4) {
            mPressed_A_box_select(p0);
        }//折線と補助活線と補助絵線
    }
//--------------------

    //マウス操作(i_mouse_modeA==3,23でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_03(origami_editor.graphic2d.point.Point p0) {
        //System.out.println("(2)zzzzz foldLines.check4_size() = "+foldLines.check4_size());
        if (i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) {
            mDragged_A_box_select(p0);
        }
        if (i_foldLine_additional == FoldLineAdditionalInputMode.BLACK_LINE_2) {
            mDragged_A_box_select(p0);
        }
        if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LIVE_LINE_3) {
            mDragged_A_box_select(p0);
        }

        if (i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) {
            mDragged_A_box_select(p0);
        }

        if (i_foldLine_additional == FoldLineAdditionalInputMode.BOTH_4) {
            mDragged_A_box_select(p0);
        }


    }

    //マウス操作(i_mouse_modeA==3,23 でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_03(origami_editor.graphic2d.point.Point p0) {//折線と補助活線と円
        //System.out.println("(3_1)zzzzz foldLines.check4_size() = "+foldLines.check4_size());
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        i_drawing_stage = 0;

        //最寄の一つを削除
        if (p19_1.distance(p0) <= 0.000001) {//最寄の一つを削除
            int i_removal_mode;//i_removal_mode is defined and declared here
            switch (i_foldLine_additional) {
                case POLY_LINE_0:
                    i_removal_mode = 0;
                    break;
                case BLACK_LINE_2:
                    i_removal_mode = 2;
                    break;
                case AUX_LIVE_LINE_3:
                    i_removal_mode = 3;
                    break;
                case AUX_LINE_1:
                    i_removal_mode = 1;
                    break;
                case BOTH_4:
                    i_removal_mode = 10;
                    double rs_min = foldLines.closestLineSegmentDistance(p);//点pに最も近い線分(折線と補助活線)の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                    double re_min = foldLines.closestCircleDistance(p);//点pに最も近い円の番号での、その距離を返す	public double mottomo_tikai_en_kyori(Ten p)
                    double hoj_rs_min = auxLines.closestLineSegmentDistance(p);//点pに最も近い補助絵線の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                    if ((rs_min <= re_min) && (rs_min <= hoj_rs_min)) {
                        if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)).getNumber() < 3) {
                            i_removal_mode = 0;
                        } else {
                            i_removal_mode = 3;
                        }
                    }
                    if ((re_min < rs_min) && (re_min <= hoj_rs_min)) {
                        i_removal_mode = 3;
                    }
                    if ((hoj_rs_min < rs_min) && (hoj_rs_min < re_min)) {
                        i_removal_mode = 1;
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }


            if (i_removal_mode == 0) { //折線の削除

                //Ten p =new Ten(); p.set(camera.TV2object(p0));
                double rs_min;
                rs_min = foldLines.closestLineSegmentDistance(p);//点pに最も近い線分(折線と補助活線)の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                if (rs_min < d_decision_width) {
                    if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)).getNumber() < 3) {
                        foldLines.deleteLineSegment_vertex(foldLines.closestLineSegmentSearchReversedOrder(p));
                        circle_organize();
                        record();
                    }
                }
            }


            if (i_removal_mode == 2) { //黒の折線の削除
                double rs_min = foldLines.closestLineSegmentDistance(p);//点pに最も近い線分(折線と補助活線)の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                if (rs_min < d_decision_width) {
                    if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)) == LineColor.BLACK_0) {
                        foldLines.deleteLineSegment_vertex(foldLines.closestLineSegmentSearchReversedOrder(p));
                        circle_organize();
                        record();
                    }
                }
            }

            if (i_removal_mode == 3) {  //補助活線
                double rs_min = foldLines.closestLineSegmentDistance(p);//点pに最も近い線分(折線と補助活線)の番号での、その距離を返す
                double re_min = foldLines.closestCircleDistance(p);//点pに最も近い円の番号での、その距離を返す	public double mottomo_tikai_en_kyori(Ten p)

                if (rs_min <= re_min) {
                    if (rs_min < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                        if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)) == LineColor.CYAN_3) {
                            foldLines.deleteLineSegment_vertex(foldLines.closestLineSegmentSearchReversedOrder(p));
                            circle_organize();
                            record();
                        }
                    }
                } else {
                    if (re_min < d_decision_width) {
                        foldLines.deleteCircle(foldLines.closest_circle_search_reverse_order(p));
                        circle_organize();
                        record();
                    }
                }
            }

            if (i_removal_mode == 1) { //補助絵線
                double rs_min;
                rs_min = auxLines.closestLineSegmentDistance(p);//点pに最も近い補助絵線の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)

                if (rs_min < d_decision_width) {
                    auxLines.deleteLineSegment_vertex(auxLines.closestLineSegmentSearchReversedOrder(p));
                    record();
                }
            }
        }


        //四角枠内の削除 //p19_1はselectの最初のTen。この条件は最初のTenと最後の点が遠いので、四角を発生させるということ。
        if (p19_1.distance(p0) > 0.000001) {
            if ((i_foldLine_additional == FoldLineAdditionalInputMode.POLY_LINE_0) || (i_foldLine_additional == FoldLineAdditionalInputMode.BOTH_4)) { //折線の削除	//D_nisuru(p19_1,p0)で折線だけが削除される。
                if (D_nisuru0(p19_1, p0) != 0) {
                    circle_organize();
                    record();
                }
            }


            if (i_foldLine_additional == FoldLineAdditionalInputMode.BLACK_LINE_2) {  //Delete only the black polygonal line
                if (D_nisuru2(p19_1, p0) != 0) {
                    circle_organize();
                    record();
                }
            }


            if ((i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LIVE_LINE_3) || (i_foldLine_additional == FoldLineAdditionalInputMode.BOTH_4)) {  //Auxiliary live line // Currently it is recorded for undo even if it is not deleted 20161218
                if (D_nisuru3(p19_1, p0) != 0) {
                    circle_organize();
                    record();
                }
            }

            if ((i_foldLine_additional == FoldLineAdditionalInputMode.AUX_LINE_1) || (i_foldLine_additional == FoldLineAdditionalInputMode.BOTH_4)) { //補助絵線	//現状では削除しないときもUNDO用に記録されてしまう20161218
                if (D_nisuru1(p19_1, p0) != 0) {
                    record();
                }
            }

        }

//qqqqqqqqqqqqqqqqqqqqqqqqqqqqq//System.out.println("= ");qqqqq
//check4(0.0001);//D_nisuru0をすると、foldLines.D_nisuru0内でresetが実行されるため、check4のやり直しが必要。
        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

    }

//--------------------

    public int D_nisuru0(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));

        return foldLines.D_nisuru0(p_a, p_b, p_c, p_d);
    }

    public int D_nisuru2(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.D_nisuru2(p_a, p_b, p_c, p_d);
    }

    public int D_nisuru3(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.D_nisuru3(p_a, p_b, p_c, p_d);
    }

    public int chenge_property_in_4kakukei(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.chenge_property_in_4kakukei(p_a, p_b, p_c, p_d, circle_custom_color);
    }

    public int D_nisuru1(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return auxLines.D_nisuru(p_a, p_b, p_c, p_d);
    }

    //59 59 59 59 59 59 59 59 59 59
    //マウス操作(i_mouse_modeA==59 "特注プロパティ指定" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_59(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);   //折線と補助活線と補助絵線
    }

    //マウス操作(i_mouse_modeA==59 "特注プロパティ指定"でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_59(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==59 "特注プロパティ指定" でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_59(origami_editor.graphic2d.point.Point p0) {//補助活線と円
        i_drawing_stage = 0;
        if (p19_1.distance(p0) > 0.000001) {//現状では削除しないときもUNDO用に記録されてしまう20161218

            if (chenge_property_in_4kakukei(p19_1, p0) != 0) {
            }
        }

        if (p19_1.distance(p0) <= 0.000001) {
            p.set(camera.TV2object(p0));
            double rs_min;
            rs_min = foldLines.closestLineSegmentDistance(p);//点pに最も近い補助活線の番号での、その距離を返す
            double re_min;
            re_min = foldLines.closestCircleDistance(p);//点pに最も近い円の番号での、その距離を返す	public double mottomo_tikai_en_kyori(Ten p)

            if (rs_min <= re_min) {
                if (rs_min < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                    if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)) == LineColor.CYAN_3) {
                        foldLines.setLineCustomized(foldLines.closestLineSegmentSearchReversedOrder(p), 1);
                        foldLines.setLineCustomizedColor(foldLines.closestLineSegmentSearchReversedOrder(p), circle_custom_color);
                        //en_seiri();kiroku();
                    }
                }
            } else {
                if (re_min < d_decision_width) {
                    foldLines.setCircleCustomized(foldLines.closest_circle_search_reverse_order(p), 1);
                    foldLines.setCircleCustomizedColor(foldLines.closest_circle_search_reverse_order(p), circle_custom_color);
                }
            }
        }
    }

    //4 4 4 4 4 444444444444444444444444444444444444444444444444444444444
    public void mPressed_A_04(origami_editor.graphic2d.point.Point p0) {
    }//マウス操作(i_mouse_modeA==4線_変換　でボタンを押したとき)時の作業

    public void mDragged_A_04(origami_editor.graphic2d.point.Point p0) {
    }//マウス操作(i_mouse_modeA==4線_変換　でドラッグしたとき)を行う関数

    //マウス操作(i_mouse_modeA==4線_変換　でボタンを離したとき)を行う関数
    public void mReleased_A_04(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
            int minrid;
            minrid = foldLines.closestLineSegmentSearch(p);
            LineColor ic_temp;
            ic_temp = foldLines.getColor(minrid);
            if (ic_temp.isFoldingLine()) {
                foldLines.setColor(minrid, ic_temp.advanceFolding());
                record();
            }
        }
    }
//--------------------

    //------
//58 58 58 58 58 58 58 58 58 58
    public void mPressed_A_58(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }//マウス操作(i_mouse_modeA==58線_変換　でボタンを押したとき)時の作業

    public void mDragged_A_58(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }//マウス操作(i_mouse_modeA==58線_変換　でドラッグしたとき)を行う関数

    //マウス操作(i_mouse_modeA==58線_変換　でボタンを離したとき)を行う関数
    public void mReleased_A_58(origami_editor.graphic2d.point.Point p0) {//ここの処理の終わりに fix2(0.001,0.5);　をするのは、元から折線だったものと、補助線から変換した折線との組合せで頻発するT字型不接続を修正するため
        i_drawing_stage = 0;

        if (p19_1.distance(p0) > 0.000001) {//
            if (MV_change(p19_1, p0) != 0) {
                fix2(0.001, 0.5);
                record();
            }
        }


        if (p19_1.distance(p0) <= 0.000001) {//
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                int minrid;
                minrid = foldLines.closestLineSegmentSearch(p);
                LineColor ic_temp;
                ic_temp = foldLines.getColor(minrid);
                if (ic_temp == LineColor.RED_1) {
                    foldLines.setColor(minrid, LineColor.BLUE_2);
                } else if (ic_temp == LineColor.BLUE_2) {
                    foldLines.setColor(minrid, LineColor.RED_1);
                }

                fix2(0.001, 0.5);
                record();
            }

        }
    }

    public int MV_change(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.MV_change(p_a, p_b, p_c, p_d);
    }
//------


//i_mouse_modeA;マウスの動作に対する反応を規定する。
// -------------1;線分入力モード。
//2;展開図調整(移動)。
//3;"L_del"
//4;"L_chan"

// -------------5;線分延長モード。
// -------------6;2点から等距離線分モード。
// -------------7;角二等分線モード。
// -------------8;内心モード。
// -------------9;垂線おろしモード。
// -------------10;折り返しモード。
// -------------11;線分入力モード。
// -------------12;鏡映モード。

//101:折り上がり図の操作。
//102;F_move
//103;S_face

//10001;test1 入力準備として点を３つ指定する


//66666666666666666666    i_mouse_modeA==6　;2点から等距離線分モード

    public void mPressed_A_30(origami_editor.graphic2d.point.Point p0) {    //マウス操作(i_mouse_modeA==4線_変換　でボタンを押したとき)時の作業
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        minrid_30 = -1;
        if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
            minrid_30 = foldLines.closestLineSegmentSearch(p);
            LineSegment s01 = new LineSegment();
            s01.set(OritaCalc.lineSegment_double(foldLines.get(minrid_30), 0.01));
            foldLines.setB(minrid_30, s01.getB());
        }
    }

    public void mDragged_A_30(origami_editor.graphic2d.point.Point p0) {//マウス操作(i_mouse_modeA==4線_変換　でドラッグしたとき)を行う関数
        if (minrid_30 > 0) {

            LineSegment s01 = new LineSegment();
            s01.set(OritaCalc.lineSegment_double(foldLines.get(minrid_30), 100.0));
            foldLines.setB(minrid_30, s01.getB());
            minrid_30 = -1;
        }

    }

    //マウス操作(i_mouse_modeA==30 除け_線_変換　でボタンを離したとき)を行う関数（背景に展開図がある場合用）
    public void mReleased_A_30(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        if (minrid_30 > 0) {

            LineSegment s01 = new LineSegment();
            s01.set(OritaCalc.lineSegment_double(foldLines.get(minrid_30), 100.0));
            foldLines.setB(minrid_30, s01.getB());

            LineColor ic_temp;
            ic_temp = foldLines.getColor(minrid_30);
            int is_temp;
            is_temp = foldLines.get_select(minrid_30);

            if ((ic_temp == LineColor.BLACK_0) && (is_temp == 0)) {
                foldLines.set_select(minrid_30, 2);
            } else if ((ic_temp == LineColor.BLACK_0) && (is_temp == 2)) {
                foldLines.setColor(minrid_30, LineColor.RED_1);
                foldLines.set_select(minrid_30, 0);
            } else if ((ic_temp == LineColor.RED_1) && (is_temp == 0)) {
                foldLines.setColor(minrid_30, LineColor.BLUE_2);
            } else if ((ic_temp == LineColor.BLUE_2) && (is_temp == 0)) {
                foldLines.setColor(minrid_30, LineColor.BLACK_0);
            }

            record();
        }


    }

//------


//------折り畳み可能線入力


//38 38 38 38 38 38 38    i_mouse_modeA==38　;折り畳み可能線入力  qqqqqqqqq

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_06(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_06(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_06(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_38(origami_editor.graphic2d.point.Point p0) {
        if (i_kou_mitudo_nyuuryoku) {
            if (i_drawing_stage == 0) {
                i_step_for_move_4p = 0;
            }

            if (i_step_for_move_4p == 0) {
                mMoved_A_29(p0);
            }

            if (i_step_for_move_4p == 1) {
                line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
                i_candidate_stage = 0;
                //Ten p =new Ten();
                p.set(camera.TV2object(p0));

                closest_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
                if ((i_drawing_stage >= 2) && (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width)) {

                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_lineSegment);//line_candidate[1].setcolor(2);
                    return;
                }
            }

            if (i_step_for_move_4p == 2) {
                i_candidate_stage = 0;
                origami_editor.graphic2d.point.Point p = new origami_editor.graphic2d.point.Point();
                p.set(camera.TV2object(p0));

                closest_lineSegment.set(getClosestLineSegment(p));
                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {//最寄の既存折線が近い場合
                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_lineSegment);
                    return;
                }

            }
        }
    }

//マウス操作(ボタンを押したとき)時の作業
    public int mPressed_A_38(origami_editor.graphic2d.point.Point p0) {//作業がすべて完了し新たな折線を追加でた場合だけ1を返す。それ以外は0を返す。
        i_candidate_stage = 0;
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        if (i_drawing_stage == 0) {
            i_step_for_move_4p = 0;
        }

        if (i_step_for_move_4p == 0) {
            double hantei_kyori = 0.000001;

            origami_editor.graphic2d.point.Point t1 = new origami_editor.graphic2d.point.Point();
            t1.set(foldLines.closestPointOfFoldLine(p));//点pに最も近い、「線分の端点」を返すori_s.mottomo_tikai_Tenは近い点がないと p_return.set(100000.0,100000.0)と返してくる

            if (p.distance(t1) < d_decision_width) {
                //t1を端点とする折線をNarabebakoに入れる
                SortingBox_int_double nbox = new SortingBox_int_double();
                for (int i = 1; i <= foldLines.getTotal(); i++) {
                    if (foldLines.getColor(i).isFoldingLine()) {
                        if (t1.distance(foldLines.getA(i)) < hantei_kyori) {
                            nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getA(i), foldLines.getB(i))));
                        } else if (t1.distance(foldLines.getB(i)) < hantei_kyori) {
                            nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getB(i), foldLines.getA(i))));
                        }
                    }
                }

                if (nbox.getTotal() % 2 == 1) {//t1を端点とする折線の数が奇数のときだけif{}内の処理をする
                    icol_temp = lineColor;
                    if (nbox.getTotal() == 1) {
                        icol_temp = foldLines.get(nbox.getInt(1)).getColor();
                    }//20180503この行追加。これは、折線が1本だけの頂点から折り畳み可能線追加機能で、その折線の延長を行った場合に、線の色を延長前の折線と合わせるため

                    //int i_kouho_suu=0;
                    for (int i = 1; i <= nbox.getTotal(); i++) {//iは角加減値を求める最初の折線のid
                        //折線が奇数の頂点周りの角加減値を2.0で割ると角加減値の最初折線と、折り畳み可能にするための追加の折線との角度になる。
                        double kakukagenti = 0.0;
                        //System.out.println("nbox.getsousuu()="+nbox.getsousuu());
                        int tikai_orisen_jyunban;
                        int tooi_orisen_jyunban;
                        for (int k = 1; k <= nbox.getTotal(); k++) {//kは角加減値を求める角度の順番
                            tikai_orisen_jyunban = i + k - 1;
                            if (tikai_orisen_jyunban > nbox.getTotal()) {
                                tikai_orisen_jyunban = tikai_orisen_jyunban - nbox.getTotal();
                            }
                            tooi_orisen_jyunban = i + k;
                            if (tooi_orisen_jyunban > nbox.getTotal()) {
                                tooi_orisen_jyunban = tooi_orisen_jyunban - nbox.getTotal();
                            }

                            double add_kakudo = OritaCalc.angle_between_0_360(nbox.getDouble(tooi_orisen_jyunban) - nbox.getDouble(tikai_orisen_jyunban));
                            if (k % 2 == 1) {
                                kakukagenti = kakukagenti + add_kakudo;
                            } else if (k % 2 == 0) {
                                kakukagenti = kakukagenti - add_kakudo;
                            }
                        }

                        if (nbox.getTotal() == 1) {
                            kakukagenti = 360.0;
                        }

                        //System.out.println("kakukagenti="+kakukagenti);
                        //チェック用に角加減値の最初の角度の中にkakukagenti/2.0があるかを確認する
                        tikai_orisen_jyunban = i;
                        if (tikai_orisen_jyunban > nbox.getTotal()) {
                            tikai_orisen_jyunban = tikai_orisen_jyunban - nbox.getTotal();
                        }
                        tooi_orisen_jyunban = i + 1;
                        if (tooi_orisen_jyunban > nbox.getTotal()) {
                            tooi_orisen_jyunban = tooi_orisen_jyunban - nbox.getTotal();
                        }

                        double add_kakudo_1 = OritaCalc.angle_between_0_360(nbox.getDouble(tooi_orisen_jyunban) - nbox.getDouble(tikai_orisen_jyunban));
                        if (nbox.getTotal() == 1) {
                            add_kakudo_1 = 360.0;
                        }

                        if ((kakukagenti / 2.0 > 0.0 + 0.000001) && (kakukagenti / 2.0 < add_kakudo_1 - 0.000001)) {
                            //if((kakukagenti/2.0>0.0-0.000001)&&(kakukagenti/2.0<add_kakudo_1+0.000001)){

                            i_drawing_stage = i_drawing_stage + 1;

                            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d)
                            LineSegment s_kiso = new LineSegment();
                            if (t1.distance(foldLines.getA(nbox.getInt(i))) < hantei_kyori) {
                                s_kiso.set(foldLines.getA(nbox.getInt(i)), foldLines.getB(nbox.getInt(i)));
                            } else if (t1.distance(foldLines.getB(nbox.getInt(i))) < hantei_kyori) {
                                s_kiso.set(foldLines.getB(nbox.getInt(i)), foldLines.getA(nbox.getInt(i)));
                            }

                            double s_kiso_length = s_kiso.getLength();

                            line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakukagenti / 2.0, grid.d_width() / s_kiso_length));
                            line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                            line_step[i_drawing_stage].setActive(LineSegment.ActiveState.INACTIVE_0);
                        }
                    }
                    if (i_drawing_stage == 1) {
                        i_step_for_move_4p = 2;
                    }
                    if (i_drawing_stage > 1) {
                        i_step_for_move_4p = 1;
                    }
                }
            }
            return 0;
        }

        if (i_step_for_move_4p == 1) {
            closest_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_step_for_move_4p = 2;
                i_drawing_stage = 1;
                line_step[1].set(closest_lineSegment);

                //i_egaki_dankai=i_egaki_dankai+1;
                //line_step[i_egaki_dankai].set(moyori_senbun);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                //line_step[i_egaki_dankai].setcolor(8);
                return 0;
            }
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {
                i_drawing_stage = 0;
                return 0;
            }
        }

        if (i_step_for_move_4p == 2) {
            closest_lineSegment.set(getClosestLineSegment(p));
            LineSegment moyori_step_lineSegment = new LineSegment();
            moyori_step_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {//最寄の既存折線が遠くて選択無効の場合
                if (OritaCalc.distance_lineSegment(p, moyori_step_lineSegment) < d_decision_width) {//最寄のstep_senbunが近い場合
                    return 0;
                }

                //最寄のstep_senbunが遠い場合
                i_drawing_stage = 0;
                return 0;
            }

            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {//最寄の既存折線が近い場合

                line_step[2].set(closest_lineSegment);
                line_step[2].setColor(LineColor.GREEN_6);

                origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
                kousa_point.set(OritaCalc.findIntersection(line_step[1], line_step[2]));
                LineSegment add_sen = new LineSegment(kousa_point, line_step[1].getA(), icol_temp);//20180503変更
                if (add_sen.getLength() > 0.00000001) {//最寄の既存折線が有効の場合
                    addLineSegment(add_sen);
                    record();
                    i_drawing_stage = 0;
                    return 1;

                }

                //最寄の既存折線が無効の場合

                //最寄のstep_senbunが近い場合
                if (OritaCalc.distance_lineSegment(p, moyori_step_lineSegment) < d_decision_width) {
                    return 0;
                }

                //最寄のstep_senbunが遠い場合
                i_drawing_stage = 0;
                return 0;
            }
        }

        return 0;
    }


//------折り畳み可能線+格子点系入力

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_38(origami_editor.graphic2d.point.Point p0) {
    }
//
//課題　step線と既存折線が平行の時エラー方向に線を引くことを改善すること20170407
//
//動作仕様
//（１）点を選択（既存点選択規制）
//（２a）選択点が3以上の奇数折線の頂点の場合
//（３）
//
//
//（２b）２a以外の場合
//
//Ten t1 =new Ten();

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_38(origami_editor.graphic2d.point.Point p0) {

    }

    //マウス操作(マウスを動かしたとき)を行う関数    //System.out.println("_");
    public void mMoved_A_39(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 0) {
            i_step_for_copy_4p = 0;
        }
        if (i_kou_mitudo_nyuuryoku) {
            i_candidate_stage = 0;
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));

            if (i_drawing_stage == 0) {
                i_step_for_copy_4p = 0;
            }
            System.out.println("i_egaki_dankai= " + i_drawing_stage + "  ;   i_step_for_copy_4p= " + i_step_for_copy_4p);

            if (i_step_for_copy_4p == 0) {
                mMoved_A_29(p0);
            }

            if (i_step_for_copy_4p == 1) {
                closest_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
                if ((i_drawing_stage >= 2) && (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width)) {
                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_lineSegment);//line_candidate[1].setcolor(2);
                    return;
                }

                closest_point.set(getClosestPoint(p));
                if (p.distance(closest_point) < d_decision_width) {
                    line_candidate[1].set(closest_point, closest_point);
                    line_candidate[1].setColor(lineColor);
                    i_candidate_stage = 1;
                    return;
                }
                return;
            }

            if (i_step_for_copy_4p == 2) {//i_step_for_copy_4p==2であれば、以下でs_step[1]を入力折線を確定する
                closest_point.set(getClosestPoint(p));

                if (closest_point.distance(line_step[1].getA()) < 0.00000001) {
                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_point, closest_point);
                    line_candidate[1].setColor(lineColor);
                    System.out.println("i_step_for39_2_   1");

                    return;
                }

                if ((p.distance(line_step[1].getB()) < d_decision_width) && (p.distance(line_step[1].getB()) <= p.distance(closest_point))) {
                    i_candidate_stage = 1;
                    line_candidate[1].set(line_step[1].getB(), line_step[1].getB());
                    line_candidate[1].setColor(lineColor);
                    System.out.println("i_step_for39_2_   2");

                    return;
                }

                if (p.distance(closest_point) < d_decision_width) {
                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_point, closest_point);
                    line_candidate[1].setColor(lineColor);
                    System.out.println("i_step_for39_2_   3");

                    return;
                }

                closest_lineSegment.set(getClosestLineSegment(p));
                LineSegment moyori_step_lineSegment = new LineSegment();
                moyori_step_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {//最寄の既存折線が遠い場合
                    if (OritaCalc.distance_lineSegment(p, moyori_step_lineSegment) < d_decision_width) {//最寄のstep_senbunが近い場合
                        return;
                    }
                    //最寄のstep_senbunが遠い場合
                    System.out.println("i_step_for39_2_   4");

                    return;
                }

                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {//最寄の既存折線が近い場合
                    i_candidate_stage = 1;
                    line_candidate[1].set(closest_lineSegment);
                    line_candidate[1].setColor(lineColor);

                    System.out.println("i_step_for39_2_   5");
                    return;
                }
                return;
            }

            return;
        }
    }

    //マウス操作(ボタンを押したとき)時の作業--------------
    public void mPressed_A_39(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {
            i_step_for_copy_4p = 0;
        }

        if (i_step_for_copy_4p == 0) {
            double decision_distance = 0.000001;

            //任意の点が与えられたとき、端点もしくは格子点で最も近い点を得る
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) < d_decision_width) {
                //moyori_tenを端点とする折線をNarabebakoに入れる
                SortingBox_int_double nbox = new SortingBox_int_double();
                for (int i = 1; i <= foldLines.getTotal(); i++) {
                    if (foldLines.getColor(i).isFoldingLine()) {
                        if (closest_point.distance(foldLines.getA(i)) < decision_distance) {
                            nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getA(i), foldLines.getB(i))));
                        } else if (closest_point.distance(foldLines.getB(i)) < decision_distance) {
                            nbox.container_i_smallest_first(new int_double(i, OritaCalc.angle(foldLines.getB(i), foldLines.getA(i))));
                        }
                    }
                }
                if (nbox.getTotal() % 2 == 1) {//moyori_tenを端点とする折線の数が奇数のときだけif{}内の処理をする
                    for (int i = 1; i <= nbox.getTotal(); i++) {//iは角加減値を求める最初の折線のid
                        //折線が奇数の頂点周りの角加減値を2.0で割ると角加減値の最初折線と、折り畳み可能にするための追加の折線との角度になる。
                        double kakukagenti = 0.0;
                        //System.out.println("nbox.getsousuu()="+nbox.getsousuu());
                        int tikai_orisen_jyunban;
                        int tooi_orisen_jyunban;
                        for (int k = 1; k <= nbox.getTotal(); k++) {//kは角加減値を求める角度の順番
                            tikai_orisen_jyunban = i + k - 1;
                            if (tikai_orisen_jyunban > nbox.getTotal()) {
                                tikai_orisen_jyunban = tikai_orisen_jyunban - nbox.getTotal();
                            }
                            tooi_orisen_jyunban = i + k;
                            if (tooi_orisen_jyunban > nbox.getTotal()) {
                                tooi_orisen_jyunban = tooi_orisen_jyunban - nbox.getTotal();
                            }

                            double add_kakudo = OritaCalc.angle_between_0_360(nbox.getDouble(tooi_orisen_jyunban) - nbox.getDouble(tikai_orisen_jyunban));
                            if (k % 2 == 1) {
                                kakukagenti = kakukagenti + add_kakudo;
                            } else if (k % 2 == 0) {
                                kakukagenti = kakukagenti - add_kakudo;
                            }
                        }

                        if (nbox.getTotal() == 1) {
                            kakukagenti = 360.0;
                        }
                        //チェック用に角加減値の最初の角度の中にkakukagenti/2.0があるかを確認する
                        tikai_orisen_jyunban = i;
                        if (tikai_orisen_jyunban > nbox.getTotal()) {
                            tikai_orisen_jyunban = tikai_orisen_jyunban - nbox.getTotal();
                        }
                        tooi_orisen_jyunban = i + 1;
                        if (tooi_orisen_jyunban > nbox.getTotal()) {
                            tooi_orisen_jyunban = tooi_orisen_jyunban - nbox.getTotal();
                        }

                        double add_kakudo_1 = OritaCalc.angle_between_0_360(nbox.getDouble(tooi_orisen_jyunban) - nbox.getDouble(tikai_orisen_jyunban));
                        if (nbox.getTotal() == 1) {
                            add_kakudo_1 = 360.0;
                        }

                        if ((kakukagenti / 2.0 > 0.0 + 0.000001) && (kakukagenti / 2.0 < add_kakudo_1 - 0.000001)) {
                            i_drawing_stage = i_drawing_stage + 1;

                            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d)
                            LineSegment s_kiso = new LineSegment();
                            if (closest_point.distance(foldLines.getA(nbox.getInt(i))) < decision_distance) {
                                s_kiso.set(foldLines.getA(nbox.getInt(i)), foldLines.getB(nbox.getInt(i)));
                            } else if (closest_point.distance(foldLines.getB(nbox.getInt(i))) < decision_distance) {
                                s_kiso.set(foldLines.getB(nbox.getInt(i)), foldLines.getA(nbox.getInt(i)));
                            }

                            double s_kiso_length = s_kiso.getLength();

                            line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakukagenti / 2.0, grid.d_width() / s_kiso_length));
                            line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                            line_step[i_drawing_stage].setActive(LineSegment.ActiveState.ACTIVE_A_1);

                        }

                    }

                    if (i_drawing_stage == 1) {
                        i_step_for_copy_4p = 2;
                    }
                    if (i_drawing_stage > 1) {
                        i_step_for_copy_4p = 1;
                    }
                }

                if (i_drawing_stage == 0) {//折畳み可能化線がない場合//System.out.println("_");
                    i_drawing_stage = 1;
                    i_step_for_copy_4p = 1;
                    line_step[1].set(closest_point, closest_point);
                    line_step[1].setColor(LineColor.PURPLE_8);
                    line_step[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
                }

            }
            return;
        }


        if (i_step_for_copy_4p == 1) {
            closest_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
            if ((i_drawing_stage >= 2) && (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width)) {
                i_step_for_copy_4p = 2;
                i_drawing_stage = 1;
                line_step[1].set(closest_lineSegment);
                return;
            }
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                line_step[1].setB(closest_point);
                i_step_for_copy_4p = 2;
                i_drawing_stage = 1;
                return;
            }
            i_drawing_stage = 0;
            i_candidate_stage = 0;
            return;
        }


        if (i_step_for_copy_4p == 2) {//i_step_for_copy_4p==2であれば、以下でs_step[1]を入力折線を確定する
            closest_point.set(getClosestPoint(p));

            if (closest_point.distance(line_step[1].getA()) < 0.00000001) {
                i_drawing_stage = 0;
                i_candidate_stage = 0;
                return;
            }

            if ((p.distance(line_step[1].getB()) < d_decision_width) &&
                    (
                            p.distance(line_step[1].getB()) <= p.distance(closest_point)
                            //moyori_ten.kyori(line_step[1].getb())<0.00000001
                    )) {
                LineSegment add_sen = new LineSegment(line_step[1].getA(), line_step[1].getB(), lineColor);
                addLineSegment(add_sen);
                record();
                i_drawing_stage = 0;
                i_candidate_stage = 0;
                return;
            }

            if (p.distance(closest_point) < d_decision_width) {
                line_step[1].setB(closest_point);
                return;
            }


            closest_lineSegment.set(getClosestLineSegment(p));

            LineSegment moyori_step_lineSegment = new LineSegment();
            moyori_step_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {//最寄の既存折線が遠い場合
                if (OritaCalc.distance_lineSegment(p, moyori_step_lineSegment) < d_decision_width) {//最寄のstep_senbunが近い場合
                    return;
                }
                //最寄のstep_senbunが遠い場合

                i_drawing_stage = 0;
                i_candidate_stage = 0;
                return;
            }

            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {//最寄の既存折線が近い場合
                line_step[2].set(closest_lineSegment);
                line_step[2].setColor(LineColor.GREEN_6);
                origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
                kousa_point.set(OritaCalc.findIntersection(line_step[1], line_step[2]));
                LineSegment add_sen = new LineSegment(kousa_point, line_step[1].getA(), lineColor);
                if (add_sen.getLength() > 0.00000001) {//最寄の既存折線が有効の場合
                    addLineSegment(add_sen);
                    record();
                    i_drawing_stage = 0;
                    i_candidate_stage = 0;
                    return;
                }
                //最寄の既存折線が無効の場合
                closest_point.set(getClosestPoint(p));
                if (p.distance(closest_point) < d_decision_width) {
                    line_step[1].setB(closest_point);
                    return;
                }
                //最寄のstep_senbunが近い場合
                if (OritaCalc.distance_lineSegment(p, moyori_step_lineSegment) < d_decision_width) {
                    return;
                }
                //最寄のstep_senbunが遠い場合
                i_drawing_stage = 0;
                i_candidate_stage = 0;
                return;

            }
            return;
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_39(origami_editor.graphic2d.point.Point p0) {
    }


//33 33 33 33 33 33 33 33 33 33 33魚の骨

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_39(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_33(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==33魚の骨　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_33(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(lineColor);
    }

    //マウス操作(i_mouse_modeA==33魚の骨　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_33(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_11(p0);
    }


//35 35 35 35 35 35 35 35 35 35 35複折り返し   入力した線分に接触している折線を折り返し　に使う

    //マウス操作(i_mouse_modeA==33魚の骨　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_33(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;

            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);

            if (p.distance(closest_point) <= d_decision_width) {  //マウスで指定した点が、最寄点と近かったときに実施
                if (line_step[1].getLength() > 0.00000001) {  //line_step[1]が、線の時（=点状ではない時）に実施
                    double dx = (line_step[1].getAX() - line_step[1].getBX()) * grid.d_width() / line_step[1].getLength();
                    double dy = (line_step[1].getAY() - line_step[1].getBY()) * grid.d_width() / line_step[1].getLength();
                    LineColor icol_temp = lineColor;
                    //int imax=;

                    origami_editor.graphic2d.point.Point pxy = new origami_editor.graphic2d.point.Point();
                    for (int i = 0; i <= (int) Math.floor(line_step[1].getLength() / grid.d_width()); i++) {

                        //System.out.println("_"+i);
                        double px = line_step[1].getBX() + (double) i * dx;
                        double py = line_step[1].getBY() + (double) i * dy;
                        pxy.set(px, py);


                        //if(pxy.kyori(foldLines.mottomo_tikai_Ten(pxy) )>0.001      )         {
                        if (foldLines.closestLineSegmentDistanceExcludingParallel(pxy, line_step[1]) > 0.001) {

                            int i_sen = 0;

                            LineSegment adds = new LineSegment(px, py, px - dy, py + dx);
                            if (kouten_ari_nasi(adds) == 1) {
                                adds.set(extendToIntersectionPoint(adds));
                                adds.setColor(icol_temp);

                                addLineSegment(adds);
                                i_sen = i_sen + 1;
                            }


                            LineSegment adds2 = new LineSegment(px, py, px + dy, py - dx);
                            if (kouten_ari_nasi(adds2) == 1) {
                                adds2.set(extendToIntersectionPoint(adds2));
                                adds2.setColor(icol_temp);

                                addLineSegment(adds2);
                                i_sen = i_sen + 1;
                            }

                            if (i_sen == 2) {
                                foldLines.del_V(pxy, d_decision_width, 0.000001);
                            }

                        }

                        if (icol_temp == LineColor.RED_1) {
                            icol_temp = LineColor.BLUE_2;
                        } else if (icol_temp == LineColor.BLUE_2) {
                            icol_temp = LineColor.RED_1;
                        }


                    }
                    record();

                }  //line_step[1]が、線の時（=点状ではない時）に実施は、ここまで
            }  //マウスで指定した点が、最寄点と近かったときに実施は、ここまで
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_35(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==35　でドラッグしたとき)を行う関数----------------------------------------------------

    //マウス操作(i_mouse_modeA==35　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_35(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(lineColor);
    }

    public void mDragged_A_35(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_11(p0);
    }


    //マウス操作(i_mouse_modeA==35　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_35(origami_editor.graphic2d.point.Point p0) {

        SortingBox_int_double nbox = new SortingBox_int_double();

        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {
                    int imax = foldLines.getTotal();
                    for (int i = 1; i <= imax; i++) {
                        LineSegment.Intersection i_lineSegment_intersection_decision = OritaCalc.line_intersect_decide_sweet(foldLines.get(i), line_step[1], 0.01, 0.01);
                        int i_jikkou = 0;
                        if (i_lineSegment_intersection_decision == LineSegment.Intersection.INTERSECTS_TSHAPE_S1_VERTICAL_BAR_25) {
                            i_jikkou = 1;
                        }//T字型 s1が縦棒
                        if (i_lineSegment_intersection_decision == LineSegment.Intersection.INTERSECTS_TSHAPE_S1_VERTICAL_BAR_26) {
                            i_jikkou = 1;
                        }//T字型 s1が縦棒

                        if (i_jikkou == 1) {
                            origami_editor.graphic2d.point.Point t_moto = new origami_editor.graphic2d.point.Point();
                            t_moto.set(foldLines.getA(i));
                            System.out.println("i_senbun_kousa_hantei_" + i_lineSegment_intersection_decision);
                            if (OritaCalc.distance_lineSegment(t_moto, line_step[1]) < OritaCalc.distance_lineSegment(foldLines.getB(i), line_step[1])) {
                                t_moto.set(foldLines.getB(i));
                            }


                            //２つの点t1,t2を通る直線に関して、点pの対照位置にある点を求める public Ten oc.sentaisyou_ten_motome(Ten t1,Ten t2,Ten p){
                            origami_editor.graphic2d.point.Point t_taisyou = new origami_editor.graphic2d.point.Point();
                            t_taisyou.set(OritaCalc.lineSymmetry_point_find(line_step[1].getA(), line_step[1].getB(), t_moto));

                            LineSegment add_sen = new LineSegment(OritaCalc.findIntersection(foldLines.get(i), line_step[1]), t_taisyou);

                            add_sen.set(extendToIntersectionPoint(add_sen));
                            add_sen.setColor(foldLines.getColor(i));
                            if (add_sen.getLength() > 0.00000001) {
                                addLineSegment(add_sen);
                            }
                        }

                    }


                    record();

                }
            }
        }
    }

    public LineSegment extendToIntersectionPoint(LineSegment s0) {//Extend s0 from point a to b, until it intersects another polygonal line. Returns a new line // Returns the same line if it does not intersect another polygonal line
        LineSegment add_sen = new LineSegment();
        add_sen.set(s0);
        origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point(1000000.0, 1000000.0); //この方法だと、エラーの原因になりうる。本当なら全線分のx_max、y_max以上の点を取ればいい。今後修正予定20161120
        double kousa_ten_kyori = kousa_point.distance(add_sen.getA());


        StraightLine tyoku1 = new StraightLine(add_sen.getA(), add_sen.getB());
        StraightLine.Intersection i_kousa_flg;
        for (int i = 1; i <= foldLines.getTotal(); i++) {
            i_kousa_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(i));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。

            if (i_kousa_flg.isIntersecting()) {
                kousa_point.set(OritaCalc.findIntersection(tyoku1, foldLines.get(i)));
                if (kousa_point.distance(add_sen.getA()) > 0.00001) {

                    if (kousa_point.distance(add_sen.getA()) < kousa_ten_kyori) {

                        double d_kakudo = OritaCalc.angle(add_sen.getA(), add_sen.getB(), add_sen.getA(), kousa_point);
                        if (d_kakudo < 1.0 || d_kakudo > 359.0) {
                            //i_kouten_ari_nasi=1;
                            kousa_ten_kyori = kousa_point.distance(add_sen.getA());
                            add_sen.set(add_sen.getA(), kousa_point);
                        }
                    }
                }
            }
        }
        return add_sen;
    }

    public LineSegment extendToIntersectionPoint_2(LineSegment s0) {//Extend s0 from point b in the opposite direction of a to the point where it intersects another polygonal line. Returns a new line // Returns the same line if it does not intersect another polygonal line
        LineSegment add_sen = new LineSegment();
        add_sen.set(s0);
        //Senbun add_sen;add_sen=s0;


        origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point(1000000.0, 1000000.0); //この方法だと、エラーの原因になりうる。本当なら全線分のx_max、y_max以上の点を取ればいい。今後修正予定20161120
        double kousa_ten_kyori = kousa_point.distance(add_sen.getA());

        StraightLine tyoku1 = new StraightLine(add_sen.getA(), add_sen.getB());
        StraightLine.Intersection i_intersection_flg;//元の線分を直線としたものと、他の線分の交差状態
        LineSegment.Intersection i_lineSegment_intersection_flg;//元の線分と、他の線分の交差状態

        System.out.println("AAAAA_");
        for (int i = 1; i <= foldLines.getTotal(); i++) {
            i_intersection_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(i));//0=この直線は与えられた線分と交差しない、1=X型で交差する、2=T型で交差する、3=線分は直線に含まれる。

            //i_lineSegment_intersection_flg=oc.senbun_kousa_hantei_amai( add_sen,foldLines.get(i),0.00001,0.00001);//20180408なぜかこの行の様にadd_senを使うと、i_senbun_kousa_flgがおかしくなる
            i_lineSegment_intersection_flg = OritaCalc.line_intersect_decide_sweet(s0, foldLines.get(i), 0.00001, 0.00001);//20180408なぜかこの行の様にs0のままだと、i_senbun_kousa_flgがおかしくならない。
            if (i_intersection_flg.isIntersecting()) {
                if (!i_lineSegment_intersection_flg.isEndpointIntersection()) {
                    //System.out.println("i_intersection_flg = "+i_intersection_flg  +      " ; i_lineSegment_intersection_flg = "+i_lineSegment_intersection_flg);
                    kousa_point.set(OritaCalc.findIntersection(tyoku1, foldLines.get(i)));
                    if (kousa_point.distance(add_sen.getA()) > 0.00001) {
                        if (kousa_point.distance(add_sen.getA()) < kousa_ten_kyori) {
                            double d_kakudo = OritaCalc.angle(add_sen.getA(), add_sen.getB(), add_sen.getA(), kousa_point);
                            if (d_kakudo < 1.0 || d_kakudo > 359.0) {
                                //i_kouten_ari_nasi=1;
                                kousa_ten_kyori = kousa_point.distance(add_sen.getA());
                                add_sen.set(add_sen.getA(), kousa_point);
                            }
                        }
                    }


                }
            }

            if (i_intersection_flg == StraightLine.Intersection.INCLUDED_3) {
                if (i_lineSegment_intersection_flg != LineSegment.Intersection.PARALLEL_EQUAL_31) {


                    System.out.println("i_intersection_flg = " + i_intersection_flg + " ; i_lineSegment_intersection_flg = " + i_lineSegment_intersection_flg);


                    kousa_point.set(foldLines.get(i).getA());
                    if (kousa_point.distance(add_sen.getA()) > 0.00001) {
                        if (kousa_point.distance(add_sen.getA()) < kousa_ten_kyori) {
                            double d_kakudo = OritaCalc.angle(add_sen.getA(), add_sen.getB(), add_sen.getA(), kousa_point);
                            if (d_kakudo < 1.0 || d_kakudo > 359.0) {
                                //i_kouten_ari_nasi=1;
                                kousa_ten_kyori = kousa_point.distance(add_sen.getA());
                                add_sen.set(add_sen.getA(), kousa_point);
                            }
                        }
                    }

                    kousa_point.set(foldLines.get(i).getB());
                    if (kousa_point.distance(add_sen.getA()) > 0.00001) {
                        if (kousa_point.distance(add_sen.getA()) < kousa_ten_kyori) {
                            double d_kakudo = OritaCalc.angle(add_sen.getA(), add_sen.getB(), add_sen.getA(), kousa_point);
                            if (d_kakudo < 1.0 || d_kakudo > 359.0) {
                                //i_kouten_ari_nasi=1;
                                kousa_ten_kyori = kousa_point.distance(add_sen.getA());
                                add_sen.set(add_sen.getA(), kousa_point);
                            }
                        }
                    }
                }
            }
        }

        add_sen.set(s0.getB(), add_sen.getB());
        return add_sen;
    }


//21 21 21 21 21    i_mouse_modeA==21　;移動モード

    public int kouten_ari_nasi(LineSegment s0) {//If s0 is extended from the point a to the b direction and intersects with another polygonal line, 0 is returned if it is not 1. The intersecting line segments at the a store have no intersection with this function.
        LineSegment add_line = new LineSegment();
        add_line.set(s0);
        origami_editor.graphic2d.point.Point intersection_point = new origami_editor.graphic2d.point.Point(1000000.0, 1000000.0); //この方法だと、エラーの原因になりうる。本当なら全線分のx_max、y_max以上の点を取ればいい。今後修正予定20161120
        double intersection_point_distance = intersection_point.distance(add_line.getA());


        StraightLine tyoku1 = new StraightLine(add_line.getA(), add_line.getB());
        StraightLine.Intersection i_intersection_flg;
        for (int i = 1; i <= foldLines.getTotal(); i++) {
            i_intersection_flg = tyoku1.lineSegment_intersect_reverse_detail(foldLines.get(i));//0 = This straight line does not intersect a given line segment, 1 = X type intersects, 2 = T type intersects, 3 = Line segment is included in the straight line.

            if (i_intersection_flg.isIntersecting()) {
                intersection_point.set(OritaCalc.findIntersection(tyoku1, foldLines.get(i)));
                if (intersection_point.distance(add_line.getA()) > 0.00001) {
                    double d_kakudo = OritaCalc.angle(add_line.getA(), add_line.getB(), add_line.getA(), intersection_point);
                    if (d_kakudo < 1.0 || d_kakudo > 359.0) {
                        return 1;

                    }

                }
            }
        }
        return 0;
    }

    //マウスを動かしたとき
    public void mMoved_A_21(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00b(p0, LineColor.MAGENTA_5);
    }//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    //マウスクリック----------------------------------------------------
    public void mPressed_A_21(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウスドラッグ----------------------------------------------------
    public void mDragged_A_21(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00b(p0, LineColor.MAGENTA_5);
    }


//-------------------------

//22 22 22 22 22    i_mouse_modeA==22　;コピペモード

    //マウスリリース----------------------------------------------------
    public void mReleased_A_21(origami_editor.graphic2d.point.Point p0) {

        i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            //やりたい動作はここに書く

            double addx, addy;
            addx = -line_step[1].getBX() + line_step[1].getAX();
            addy = -line_step[1].getBY() + line_step[1].getAY();

            FoldLineSet ori_s_temp = new FoldLineSet();    //セレクトされた折線だけ取り出すために使う
            ori_s_temp.setMemo(foldLines.getMemoSelectOption(2));//セレクトされた折線だけ取り出してori_s_tempを作る
            foldLines.delSelectedLineSegmentFast();//セレクトされた折線を削除する。
            ori_s_temp.move(addx, addy);//全体を移動する

            int sousuu_old = foldLines.getTotal();
            foldLines.addMemo(ori_s_temp.getMemo());
            int sousuu_new = foldLines.getTotal();
            foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, sousuu_new);

            foldLines.unselect_all();
            record();

            app.i_mouse_modeA = MouseMode.CREASE_SELECT_19;//20200930 add セレクトした折線に作業して、その後またセレクトできる状態に戻すための行
        }
    }

    //マウスを動かしたとき
    public void mMoved_A_22(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00b(p0, LineColor.MAGENTA_5);
    }//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    //マウスクリック----------------------------------------------------
    public void mPressed_A_22(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウスドラッグ----------------------------------------------------
    public void mDragged_A_22(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00b(p0, LineColor.MAGENTA_5);
    }


//--------------------------------------------
//31 31 31 31 31 31 31 31  i_mouse_modeA==31move2p2p	入力 31 31 31 31 31 31 31 31

//動作概要　
//i_mouse_modeA==1と線分分割以外は同じ　
//

    //マウスリリース----------------------------------------------------
    public void mReleased_A_22(origami_editor.graphic2d.point.Point p0) {
        i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            //やりたい動作はここに書く

            double addx, addy;
            addx = -line_step[1].getBX() + line_step[1].getAX();
            addy = -line_step[1].getBY() + line_step[1].getAY();

            FoldLineSet ori_s_temp = new FoldLineSet();    //セレクトされた折線だけ取り出すために使う
            ori_s_temp.setMemo(foldLines.getMemoSelectOption(2));//セレクトされた折線だけ取り出してori_s_tempを作る
            //foldLines.del_selected_senbun_hayai();//セレクトされた折線を削除する。moveと　copyの違いはこの行が有効かどうかの違い
            ori_s_temp.move(addx, addy);//全体を移動する

            int sousuu_old = foldLines.getTotal();
            foldLines.addMemo(ori_s_temp.getMemo());
            int sousuu_new = foldLines.getTotal();
            foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, sousuu_new);

            foldLines.unselect_all();
            record();

            app.i_mouse_modeA = MouseMode.CREASE_SELECT_19;//20200930 add セレクトした折線に作業して、その後またセレクトできる状態に戻すための行
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_31(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==31move2p2p　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_31(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第1段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
            }
            return;
        }

        if (i_drawing_stage == 1) {    //第2段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                //点の選択が失敗した場合もi_select_mode=0にしないと、セレクトのつもりが動作モードがmove2p2pになったままになる
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            if (OritaCalc.distance(line_step[1].getA(), line_step[2].getA()) < 0.00000001) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
            }
            return;
        }


        if (i_drawing_stage == 2) {    //第3段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

                return;

            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            return;
        }


        if (i_drawing_stage == 3) {    //第4段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            if (OritaCalc.distance(line_step[3].getA(), line_step[4].getA()) < 0.00000001) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

            }
            return;
        }
    }

    //マウス操作(i_mouse_modeA==31move2p2p　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_31(origami_editor.graphic2d.point.Point p0) {
    }

//  ********************************************


//--------------------------------------------
//32 32 32 32 32 32 32 32  i_mouse_modeA==32copy2p2p	入力 32 32 32 32 32 32 32 32

//動作概要　
//i_mouse_modeA==1と線分分割以外は同じ　
//

    //マウス操作(i_mouse_modeA==31move2p2p　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_31(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 4) {
            i_drawing_stage = 0;
            i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

            FoldLineSet ori_s_temp = new FoldLineSet();    //セレクトされた折線だけ取り出すために使う
            ori_s_temp.setMemo(foldLines.getMemoSelectOption(2));//セレクトされた折線だけ取り出してori_s_tempを作る
            foldLines.delSelectedLineSegmentFast();//セレクトされた折線を削除する。
            ori_s_temp.move(line_step[1].getA(), line_step[2].getA(), line_step[3].getA(), line_step[4].getA());//全体を移動する

            int sousuu_old = foldLines.getTotal();
            foldLines.addMemo(ori_s_temp.getMemo());
            int sousuu_new = foldLines.getTotal();
            foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, sousuu_new);

            foldLines.unselect_all();
            record();
            app.i_mouse_modeA = MouseMode.CREASE_SELECT_19;//20200930 add セレクトした折線に作業して、その後またセレクトできる状態に戻すための行
        }
    }

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_32(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==32copy2p2p2p2p　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_32(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第1段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
            }
            return;
        }

        if (i_drawing_stage == 1) {    //第2段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            if (OritaCalc.distance(line_step[1].getA(), line_step[2].getA()) < 0.00000001) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
            }
            return;
        }


        if (i_drawing_stage == 2) {    //第3段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            return;
        }

        if (i_drawing_stage == 3) {    //第4段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));

            }
            if (OritaCalc.distance(line_step[3].getA(), line_step[4].getA()) < 0.00000001) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
            }
            return;
        }
    }

    //マウス操作(i_mouse_modeA==32copy2p2p　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_32(origami_editor.graphic2d.point.Point p0) {
    }

//  ********************************************

    //マウス操作(i_mouse_modeA==32copy2p2pp　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_32(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 4) {
            i_drawing_stage = 0;
            i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。

            FoldLineSet ori_s_temp = new FoldLineSet();    //セレクトされた折線だけ取り出すために使う
            ori_s_temp.setMemo(foldLines.getMemoSelectOption(2));//セレクトされた折線だけ取り出してori_s_tempを作る
            ori_s_temp.move(line_step[1].getA(), line_step[2].getA(), line_step[3].getA(), line_step[4].getA());//全体を移動する

            int sousuu_old = foldLines.getTotal();
            foldLines.addMemo(ori_s_temp.getMemo());
            int sousuu_new = foldLines.getTotal();
            foldLines.intersect_divide(1, sousuu_old, sousuu_old + 1, sousuu_new);

            record();
            app.i_mouse_modeA = MouseMode.CREASE_SELECT_19;//20200930 add セレクトした折線に作業して、その後またセレクトできる状態に戻すための行
        }
    }

    //12 12 12 12 12    i_mouse_modeA==12　;鏡映モード
    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_12(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==12鏡映モード　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_12(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第1段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
            }
            return;
        }

        if (i_drawing_stage == 1) {    //第2段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
                line_step[1].setB(line_step[2].getB());
            }
            if (line_step[1].getLength() < 0.00000001) {
                i_drawing_stage = 0;
                i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
            }
        }
    }

    //マウス操作(i_mouse_modeA==12鏡映モード　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_12(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(i_mouse_modeA==12鏡映モード　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_12(origami_editor.graphic2d.point.Point p0) {
        LineSegment adds = new LineSegment();
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            i_select_mode = 0;//  <-------20180919この行はセレクトした線の端点を選ぶと、移動とかコピー等をさせると判断するが、その操作が終わったときに必要だから追加した。
            int old_sousuu = foldLines.getTotal();

            for (int i = 1; i <= foldLines.getTotal(); i++) {
                if (foldLines.get_select(i) == 2) {
                    adds.set(OritaCalc.sentaisyou_lineSegment_motome(foldLines.get(i), line_step[1]));
                    adds.setColor(foldLines.getColor(i));

                    foldLines.addLine(adds.getA(), adds.getB());
                    foldLines.setColor(foldLines.getTotal(), foldLines.getColor(i));
                }
            }

            int new_sousuu = foldLines.getTotal();

            foldLines.intersect_divide(1, old_sousuu, old_sousuu + 1, new_sousuu);

            foldLines.unselect_all();
            record();
            app.i_mouse_modeA = MouseMode.CREASE_SELECT_19;//20200930 add セレクトした折線に作業して、その後またセレクトできる状態に戻すための行
        }
    }

//34 34 34 34 34 34 34 34 34 34 34入力した線分に重複している折線を順に山谷にする

    //-------------------------
    public void del_selected_senbun() {
        foldLines.delSelectedLineSegmentFast();
    }

    public void mMoved_A_34(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==34　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_34(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(lineColor);
    }

    //マウス操作(i_mouse_modeA==34　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_34(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_11(p0);
    }


//64 64 64 64 64 64 64 64 64 64 64 64 64入力した線分に重複している折線を削除する

    //マウス操作(i_mouse_modeA==34　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_34(origami_editor.graphic2d.point.Point p0) {

        SortingBox_int_double nbox = new SortingBox_int_double();

        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {
                    for (int i = 1; i <= foldLines.getTotal(); i++) {
                        if (OritaCalc.lineSegmentoverlapping(foldLines.get(i), line_step[1])) {
                            int_double i_d = new int_double(i, OritaCalc.distance_lineSegment(line_step[1].getB(), foldLines.get(i)));
                            nbox.container_i_smallest_first(i_d);
                        }

                    }

                    LineColor icol_temp = lineColor;

                    for (int i = 1; i <= nbox.getTotal(); i++) {

                        foldLines.setColor(nbox.getInt(i), icol_temp);


                        if (icol_temp == LineColor.RED_1) {
                            icol_temp = LineColor.BLUE_2;
                        } else if (icol_temp == LineColor.BLUE_2) {
                            icol_temp = LineColor.RED_1;
                        }
                    }
                    record();
                }
            }
        }
    }

    public void mMoved_A_64(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_11(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==64　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_64(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(LineColor.MAGENTA_5);
    }

    //マウス操作(i_mouse_modeA==64　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_64(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_11(p0);
    }


//65 65 65 65 65 65 65 65 65 65 65 65 65入力した線分に重複している折線やX交差している折線を削除する

    //マウス操作(i_mouse_modeA==64　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_64(origami_editor.graphic2d.point.Point p0) {

        SortingBox_int_double nbox = new SortingBox_int_double();

        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {

                    foldLines.D_nisuru_line(line_step[1], "l");//lは小文字のエル

                    record();

                }
            }
        }

    }

    //マウスを動かしたとき
    public void mMoved_A_65(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00b(p0, LineColor.MAGENTA_5);
    }//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    //マウスクリック----------------------------------------------------
    public void mPressed_A_65(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウスドラッグ----------------------------------------------------
    public void mDragged_A_65(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00b(p0, LineColor.MAGENTA_5);
    }

//----------------------------------------------------------------------------------------
//多角形を入力(既存頂点への引き寄せあるが既存頂点が遠い場合は引き寄せ無し)し、何らかの作業を行うセット
    //マウス操作(マウスを動かしたとき)を行う関数

    //マウス操作(i_mouse_modeA==65　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_65(origami_editor.graphic2d.point.Point p0) {

        i_drawing_stage = 0;
        p.set(camera.TV2object(p0));

        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            //やりたい動作はここに書く
            foldLines.D_nisuru_line(line_step[1], "lX");//lXは小文字のエルと大文字のエックス
            record();
        }
    }

    public void mMoved_takakukei_and_sagyou(origami_editor.graphic2d.point.Point p0) {
        //マウス操作(マウスを動かしたとき)を行う関数
//	public void mMoved_m_002(Ten p0,int i_c) //マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。
        if (i_kou_mitudo_nyuuryoku) {
            line_candidate[1].setActive(LineSegment.ActiveState.ACTIVE_BOTH_3);
            p.set(camera.TV2object(p0));
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) > p.distance(line_step[1].getA())) {
                closest_point.set(line_step[1].getA());
            }

            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            line_candidate[1].setColor(LineColor.MAGENTA_5);
            //return;
        }

    }

    //マウス操作(ボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_takakukei_and_sagyou(origami_editor.graphic2d.point.Point p0) {
//i_egaki_dankai==0なのはこの操作ボタンを押した直後の段階か、多角形が完成して、その後ボタンを押した後
        if (i_takakukei_kansei == 1) {
            i_takakukei_kansei = 0;
            i_drawing_stage = 0;
        }

        i_drawing_stage = i_drawing_stage + 1;
        line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 1) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) > d_decision_width) {
                closest_point.set(p);
            }
            line_step[i_drawing_stage].set(closest_point, p);

        } else {//ここでi_egaki_dankai=0となることはない。
            line_step[i_drawing_stage].set(line_step[i_drawing_stage - 1].getB(), p);
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数----------------------------------------------------

    public void mDragged_takakukei_and_sagyou(origami_editor.graphic2d.point.Point p0) {
        //if(i_takakukei_kansei==0)//ここにくるときは必ずi_takakukei_kansei==0なのでif分は無意味

        p.set(camera.TV2object(p0));
        line_step[i_drawing_stage].setB(p);


        if (i_kou_mitudo_nyuuryoku) {
            i_candidate_stage = 1;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) > p.distance(line_step[1].getA())) {
                closest_point.set(line_step[1].getA());
            }


            if (p.distance(closest_point) < d_decision_width) {
                line_candidate[1].set(closest_point, closest_point);
            } else {
                line_candidate[1].set(p, p);
            }

            line_step[i_drawing_stage].setB(line_candidate[1].getA());
        }
    }

    //マウス操作(ボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_takakukei_and_sagyou(origami_editor.graphic2d.point.Point p0, int i_mode) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            closest_point.set(p);
        }

        line_step[i_drawing_stage].setB(closest_point);


        if (i_drawing_stage >= 2) {
            if (p.distance(line_step[1].getA()) <= d_decision_width) {
                line_step[i_drawing_stage].setB(line_step[1].getA());
                //i_O_F_C=1;
                i_takakukei_kansei = 1;
            }
        }

        if (i_takakukei_kansei == 1) {
            Polygon Taka = new Polygon(i_drawing_stage);
            for (int i = 1; i <= i_drawing_stage; i++) {
                Taka.set(i, line_step[i].getA());
            }

            //各動作モードで独自に行う作業は以下に条件分けして記述する
            if (i_mode == 66) {
                foldLines.select_Takakukei(Taka, "select");
            }//66 66 66 66 66 多角形を入力し、それに全体が含まれる折線をselectする
            if (i_mode == 67) {
                foldLines.select_Takakukei(Taka, "unselect");
            }//67 67 67 67 67 多角形を入力し、それに全体が含まれる折線を折線をunselectする
            //各動作モードで独自に行う作業はここまで
        }
    }


//20201024高密度入力がオンならばapのrepaint（画面更新）のたびにTen kus_sisuu=new Ten(es1.get_moyori_ten_sisuu(p_mouse_TV_iti));で最寄り点を求めているので、この描き職人内で別途最寄り点を求めていることは二度手間になっている。

    //66 66 66 66 66 多角形を入力し、それに全体が含まれる折線をselectする
    public void mMoved_A_66(origami_editor.graphic2d.point.Point p0) {
        mMoved_takakukei_and_sagyou(p0);
    }    //マウス操作(マウスを動かしたとき)を行う関数

    public void mPressed_A_66(origami_editor.graphic2d.point.Point p0) {
        mPressed_takakukei_and_sagyou(p0);
    }    //マウス操作でボタンを押したとき)時の作業----------------------------------------------------

    public void mDragged_A_66(origami_editor.graphic2d.point.Point p0) {
        mDragged_takakukei_and_sagyou(p0);
    }    //マウス操作(ドラッグしたとき)を行う関数----------------------------------------------------

    public void mReleased_A_66(origami_editor.graphic2d.point.Point p0) {
        mReleased_takakukei_and_sagyou(p0, 66);
    }    //マウス操作(ボタンを離したとき)を行う関数----------------------------------------------------

    //67 67 67 67 67 多角形を入力し、それに全体が含まれる折線を折線をunselectする
    public void mMoved_A_67(origami_editor.graphic2d.point.Point p0) {
        mMoved_takakukei_and_sagyou(p0);
    }    //マウス操作(マウスを動かしたとき)を行う関数

    public void mPressed_A_67(origami_editor.graphic2d.point.Point p0) {
        mPressed_takakukei_and_sagyou(p0);
    }    //マウス操作でボタンを押したとき)時の作業----------------------------------------------------

    public void mDragged_A_67(origami_editor.graphic2d.point.Point p0) {
        mDragged_takakukei_and_sagyou(p0);
    }    //マウス操作(ドラッグしたとき)を行う関数----------------------------------------------------

    public void mReleased_A_67(origami_editor.graphic2d.point.Point p0) {
        mReleased_takakukei_and_sagyou(p0, 67);
    }    //マウス操作(ボタンを離したとき)を行う関数----------------------------------------------------


//68 68 68 68 68 入力した線分に重複している折線やX交差している折線をselectする

    //マウスを動かしたとき
    public void mMoved_A_68(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00b(p0, LineColor.MAGENTA_5);
    }//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    //マウスクリック----------------------------------------------------
    public void mPressed_A_68(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウスドラッグ----------------------------------------------------
    public void mDragged_A_68(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウス操作でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_68(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            //やりたい動作はここに書く
            foldLines.select_lX(line_step[1], "select_lX");//lXは小文字のエルと大文字のエックス
        }
    }


//69 69 69 69 69 入力した線分に重複している折線やX交差している折線をunselectする

    //マウスを動かしたとき
    public void mMoved_A_69(origami_editor.graphic2d.point.Point p0) {
        mMoved_m_00b(p0, LineColor.MAGENTA_5);
    }//マウスで選択できる候補点を表示する。近くに既成の点があるときはその点、無いときはマウスの位置自身が候補点となる。

    //マウスクリック----------------------------------------------------
    public void mPressed_A_69(origami_editor.graphic2d.point.Point p0) {
        mPressed_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウスドラッグ----------------------------------------------------
    public void mDragged_A_69(origami_editor.graphic2d.point.Point p0) {
        mDragged_m_00b(p0, LineColor.MAGENTA_5);
    }

    //マウス操作でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_69(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        line_step[1].setA(p);
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) <= d_decision_width) {
            line_step[1].setA(closest_point);
        }
        if (line_step[1].getLength() > 0.00000001) {
            //やりたい動作はここに書く
            foldLines.select_lX(line_step[1], "unselect_lX");//lXは小文字のエルと大文字のエックス
        }
    }


//36 36 36 36 36 36 36 36 36 36 36入力した線分にX交差している折線を順に山谷にする

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_36(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_28(p0);
    }//近い既存点のみ表示

    //マウス操作(i_mouse_modeA==36　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_36(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            closest_point.set(p);
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(lineColor);
    }

    //マウス操作(i_mouse_modeA==36　でドラッグしたとき)を行う関数----------------------------------------------------

    public void mDragged_A_36(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_28(p0);
    }

    //マウス操作(i_mouse_modeA==36　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_36(origami_editor.graphic2d.point.Point p0) {
        SortingBox_int_double nbox = new SortingBox_int_double();

        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) > d_decision_width) {
                closest_point.set(p);
            }
            line_step[1].setA(closest_point);
            if (line_step[1].getLength() > 0.00000001) {
                for (int i = 1; i <= foldLines.getTotal(); i++) {
                    LineSegment.Intersection i_senbun_kousa_hantei = OritaCalc.line_intersect_decide(foldLines.get(i), line_step[1], 0.0001, 0.0001);
                    int i_jikkou = 0;
                    if (i_senbun_kousa_hantei == LineSegment.Intersection.INTERSECTS_1) {
                        i_jikkou = 1;
                    }
                    if (i_senbun_kousa_hantei == LineSegment.Intersection.INTERSECTS_TSHAPE_S2_VERTICAL_BAR_27) {
                        i_jikkou = 1;
                    }
                    if (i_senbun_kousa_hantei == LineSegment.Intersection.INTERSECTS_TSHAPE_S2_VERTICAL_BAR_28) {
                        i_jikkou = 1;
                    }

                    if (i_jikkou == 1) {
                        int_double i_d = new int_double(i, OritaCalc.distance(line_step[1].getB(), OritaCalc.findIntersection(foldLines.get(i), line_step[1])));
                        nbox.container_i_smallest_first(i_d);
                    }

                }

                System.out.println("i_d_sousuu" + nbox.getTotal());

                LineColor icol_temp = lineColor;

                for (int i = 1; i <= nbox.getTotal(); i++) {

                    foldLines.setColor(nbox.getInt(i), icol_temp);


                    if (icol_temp == LineColor.RED_1) {
                        icol_temp = LineColor.BLUE_2;
                    } else if (icol_temp == LineColor.BLUE_2) {
                        icol_temp = LineColor.RED_1;
                    }
                }


                record();

            }
        }
    }

//63 63 63 外周部の折り畳みチェック


    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_63(origami_editor.graphic2d.point.Point p0) {
    }


//lineColor=3 cyan
//lineColor=4 orange
//lineColor=5 mazenta
//lineColor=6 green
//lineColor=7 yellow

    //マウス操作(i_mouse_modeA==63　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_63(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 0) {
            i_O_F_C = false;
            i_drawing_stage = i_drawing_stage + 1;

            p.set(camera.TV2object(p0));
            line_step[i_drawing_stage].set(p, p);
            line_step[i_drawing_stage].setColor(LineColor.YELLOW_7);
        } else {
            if (!i_O_F_C) {
                i_drawing_stage = i_drawing_stage + 1;
                p.set(camera.TV2object(p0));
                line_step[i_drawing_stage].set(line_step[i_drawing_stage - 1].getB(), p);
                line_step[i_drawing_stage].setColor(LineColor.YELLOW_7);
            }
        }

    }


    //マウス操作(i_mouse_modeA==63　でドラッグしたとき)を行う関数----------------------------------------------------

    public void mDragged_A_63(origami_editor.graphic2d.point.Point p0) {
        if (!i_O_F_C) {
            p.set(camera.TV2object(p0));
            line_step[i_drawing_stage].setB(p);
        }
    }

    //マウス操作(i_mouse_modeA==63　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_63(origami_editor.graphic2d.point.Point p0) {


        if (!i_O_F_C) {
            p.set(camera.TV2object(p0));
            line_step[i_drawing_stage].setB(p);


            if (p.distance(line_step[1].getA()) <= d_decision_width) {
                line_step[i_drawing_stage].setB(line_step[1].getA());
                i_O_F_C = true;
            }


            if (i_O_F_C) {
                if (i_drawing_stage == 2) {
                    i_drawing_stage = 0;
                }
            }


        }

        int i_tekisetu = 1;//外周部の黄色い線と外周部の全折線の交差が適切（全てX型の交差）なら1、1つでも適切でないなら0
        if (i_O_F_C) {
            SortingBox_int_double goukei_nbox = new SortingBox_int_double();
            SortingBox_int_double nbox = new SortingBox_int_double();
            for (int i_s_step = 1; i_s_step <= i_drawing_stage; i_s_step++) {
                nbox.reset();
                for (int i = 1; i <= foldLines.getTotal(); i++) {

                    LineSegment.Intersection i_senbun_kousa_hantei = OritaCalc.line_intersect_decide(foldLines.get(i), line_step[i_s_step], 0.0001, 0.0001);
                    int i_jikkou = 0;

                    if ((i_senbun_kousa_hantei != LineSegment.Intersection.NO_INTERSECTION_0) && (i_senbun_kousa_hantei != LineSegment.Intersection.INTERSECTS_1)) {
                        i_tekisetu = 0;
                    }

                    if (i_senbun_kousa_hantei == LineSegment.Intersection.INTERSECTS_1) {
                        i_jikkou = 1;
                    }

                    if (foldLines.get(i).getColor().getNumber() >= 3) {
                        i_jikkou = 0;
                    }


                    if (i_jikkou == 1) {
                        int_double i_d = new int_double(i, OritaCalc.distance(line_step[i_s_step].getA(), OritaCalc.findIntersection(foldLines.get(i), line_step[i_s_step])));
                        nbox.container_i_smallest_first(i_d);
                    }
                }


                for (int i = 1; i <= nbox.getTotal(); i++) {
                    int_double i_d = new int_double(nbox.getInt(i), goukei_nbox.getTotal());
                    goukei_nbox.container_i_smallest_first(i_d);
                }


            }
            System.out.println(" --------------------------------");

            if (i_tekisetu == 1) {

                LineColor i_hantai_color = LineColor.MAGENTA_5;//判定結果を表す色番号。5（マゼンタ、赤紫）は折畳不可。3（シアン、水色）は折畳可。

                if (goukei_nbox.getTotal() % 2 != 0) {//外周部として選択した折線の数が奇数
                    i_hantai_color = LineColor.MAGENTA_5;
                } else if (goukei_nbox.getTotal() == 0) {//外周部として選択した折線の数が0
                    i_hantai_color = LineColor.CYAN_3;
                } else {//外周部として選択した折線の数が偶数
                    LineSegment s_idou = new LineSegment();
                    s_idou.set(foldLines.get(goukei_nbox.getInt(1)));

                    for (int i = 2; i <= goukei_nbox.getTotal(); i++) {
                        s_idou.set(OritaCalc.sentaisyou_lineSegment_motome(s_idou, foldLines.get(goukei_nbox.getInt(i))));
                    }
                    i_hantai_color = LineColor.MAGENTA_5;
                    if (OritaCalc.equal(foldLines.get(goukei_nbox.getInt(1)).getA(), s_idou.getA(), 0.0001)) {
                        if (OritaCalc.equal(foldLines.get(goukei_nbox.getInt(1)).getB(), s_idou.getB(), 0.0001)) {
                            i_hantai_color = LineColor.CYAN_3;
                        }
                    }
                }

                for (int i_s_step = 1; i_s_step <= i_drawing_stage; i_s_step++) {
                    line_step[i_s_step].setColor(i_hantai_color);
                }

                System.out.println(" --------------------------------");
            }
        }
    }


//--------------------------------------------------------------------------------
//13 13 13 13 13 13    i_mouse_modeA==13　;角度系モード//線分指定、交点まで

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_13(origami_editor.graphic2d.point.Point p0) {

        int honsuu = 0;//1つの端点周りに描く線の本数
        if (id_angle_system != 0) {
            honsuu = id_angle_system * 2 - 1;
        } else if (id_angle_system == 0) {
            honsuu = 6;
        }

        int i_jyunnbi_step_suu = 1;//動作の準備として人間が選択するステップ数

        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第１段階として、線分を選択
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = 1;
                line_step[1].set(closest_lineSegment);
                line_step[1].setColor(LineColor.MAGENTA_5);
            }
        }

        if (i_drawing_stage == i_jyunnbi_step_suu) {    //if(i_egaki_dankai==1){        //動作の準備として人間が選択するステップ数が終わった状態で実行
            int i_jyun;//i_jyunは線を描くとき順番に色を変えたいとき使う
            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d) //    double d_angle_system;double angle;

            if (id_angle_system != 0) {
                d_angle_system = 180.0 / (double) id_angle_system;
            } else if (id_angle_system == 0) {
                d_angle_system = 180.0 / 4.0;
            }

            if (id_angle_system != 0) {
                LineSegment s_kiso = new LineSegment(line_step[1].getA(), line_step[1].getB());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = angle + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }

                s_kiso.set(line_step[1].getB(), line_step[1].getA());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = angle + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }
            }
            if (id_angle_system == 0) {
                double[] jk = new double[7];
                jk[0] = 0.0;
                jk[1] = d_restricted_angle_2;
                jk[2] = d_restricted_angle_1;
                jk[3] = d_restricted_angle_3;
                jk[4] = 360.0 - d_restricted_angle_2;
                jk[5] = 360.0 - d_restricted_angle_1;
                jk[6] = 360.0 - d_restricted_angle_3;

                LineSegment s_kiso = new LineSegment(line_step[1].getA(), line_step[1].getB());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= 6; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }

                s_kiso.set(line_step[1].getB(), line_step[1].getA());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= 6; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }
            }


            return;
        }


        if (i_drawing_stage == i_jyunnbi_step_suu + (honsuu) + (honsuu)) {//19     //動作の準備としてソフトが返答するステップ数が終わった状態で実行

            int i_tikai_s_step_suu = 0;

            //line_step[2から10]までとs_step[11から19]まで
            closest_lineSegment.set(get_moyori_step_senbun(p, 2, 1 + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                i_tikai_s_step_suu = i_tikai_s_step_suu + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);    //line_step[i_egaki_dankai].setcolor(2);//line_step[20]にinput
            }

            //line_step[2から10]までとs_step[11から19]まで
            closest_lineSegment.set(get_moyori_step_senbun(p, 1 + (honsuu) + 1, 1 + (honsuu) + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                i_tikai_s_step_suu = i_tikai_s_step_suu + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);    //line_step[i_egaki_dankai].setcolor(lineColor);
            }

            if (i_tikai_s_step_suu == 2) { //この段階でs_stepが[21]までうまってたら、line_step[20]とs_step[21]は共に加える折線なので、ここで処理を終えてしまう。
                //=     1+ (honsuu) +(honsuu) +  2 ){i_egaki_dankai=0; //この段階でs_stepが[21]までうまってたら、line_step[20]とs_step[21]は共に加える折線なので、ここで処理を終えてしまう。
                //例外処理としてs_step[20]とs_step[21]が平行の場合、より近いほうをs_stepが[20]とし、s_stepを[20]としてリターン（この場合はまだ処理は終われない）。
                //２つの線分が平行かどうかを判定する関数。oc.heikou_hantei(Tyokusen t1,Tyokusen t2)//0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する
                //0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する

                if (OritaCalc.parallel_judgement(line_step[i_drawing_stage - 1], line_step[i_drawing_stage], 0.1) != OritaCalc.ParallelJudgement.NOT_PARALLEL) {//ここは安全を見て閾値を0.1と大目にとっておこのがよさそう
                    i_drawing_stage = 0;
                    return;
                }


                i_drawing_stage = 0;

                //line_step[20]とs_step[21]の交点はoc.kouten_motome(Senbun s1,Senbun s2)で求める//２つの線分を直線とみなして交点を求める関数。線分としては交差しなくても、直線として交差している場合の交点を返す
                origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
                kousa_point.set(OritaCalc.findIntersection(line_step[1 + (honsuu) + (honsuu) + 1], line_step[1 + (honsuu) + (honsuu) + 1 + 1]));

                LineSegment add_sen = new LineSegment(kousa_point, line_step[1 + (honsuu) + (honsuu) + 1].getA());
                add_sen.setColor(lineColor);
                if (add_sen.getLength() > 0.00000001) {
                    addLineSegment(add_sen);
                }

                LineSegment add_sen2 = new LineSegment(kousa_point, line_step[1 + (honsuu) + (honsuu) + 1 + 1].getA());
                add_sen2.setColor(lineColor);
                if (add_sen.getLength() > 0.00000001) {
                    addLineSegment(add_sen2);
                }
                record();
            }

            i_drawing_stage = 0;
            return;
        }
        return;
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_13(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_13(origami_editor.graphic2d.point.Point p0) {
    }

//------


//--------------------------------------------------------------------------------
//17 17 17 17 17 17    i_mouse_modeA==17　;角度系モード

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_17(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage <= 1) {
            mMoved_A_11(p0);//近い既存点のみ表示
        }
    }

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_17(origami_editor.graphic2d.point.Point p0) {

        int honsuu = 0;//1つの端点周りに描く線の本数
        if (id_angle_system != 0) {
            honsuu = id_angle_system * 2 - 1;
        } else if (id_angle_system == 0) {
            honsuu = 6;
        }

        int i_jyunnbi_step_suu = 2;//動作の準備として人間が選択するステップ数

        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 0) {    //第1段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.MAGENTA_5);
            }
            return;
        }

        if (i_drawing_stage == 1) {    //第2段階として、点を選択
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 0;
                return;
            }
            if (p.distance(closest_point) < d_decision_width) {

                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
                line_step[1].setB(line_step[2].getB());


            }

        }

        if (i_drawing_stage == i_jyunnbi_step_suu) {    //if(i_egaki_dankai==1){        //動作の準備として人間が選択するステップ数が終わった状態で実行
            int i_jyun;//i_jyunは線を描くとき順番に色を変えたいとき使う
            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d) //    double d_angle_system;double angle;

            if (id_angle_system != 0) {
                d_angle_system = 180.0 / (double) id_angle_system;
            } else if (id_angle_system == 0) {
                d_angle_system = 180.0 / 4.0;
            }

            if (id_angle_system != 0) {

                LineSegment s_kiso = new LineSegment(line_step[1].getA(), line_step[1].getB());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = angle + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }

                s_kiso.set(line_step[1].getB(), line_step[1].getA());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = angle + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }
            }
            if (id_angle_system == 0) {
                double[] jk = new double[7];
                jk[0] = 0.0;
                jk[1] = d_restricted_angle_2;
                jk[2] = d_restricted_angle_1;
                jk[3] = d_restricted_angle_3;
                jk[4] = 360.0 - d_restricted_angle_2;
                jk[5] = 360.0 - d_restricted_angle_1;
                jk[6] = 360.0 - d_restricted_angle_3;

                LineSegment s_kiso = new LineSegment(line_step[1].getA(), line_step[1].getB());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= 6; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }

                s_kiso.set(line_step[1].getB(), line_step[1].getA());
                angle = 0.0;
                i_jyun = 0;
                for (int i = 1; i <= 6; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    angle = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, angle, 10.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }
            }


            return;
        }


        if (i_drawing_stage == i_jyunnbi_step_suu + (honsuu) + (honsuu)) {//19     //動作の準備としてソフトが返答するステップ数が終わった状態で実行

            int i_tikai_s_step_suu = 0;

            //line_step[2から10]までとs_step[11から19]まで
            closest_lineSegment.set(get_moyori_step_senbun(p, 3, 2 + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                i_tikai_s_step_suu = i_tikai_s_step_suu + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);    //line_step[i_egaki_dankai].setcolor(2);//line_step[20]にinput
            }

            //line_step[2から10]までとs_step[11から19]まで
            closest_lineSegment.set(get_moyori_step_senbun(p, 2 + (honsuu) + 1, 2 + (honsuu) + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                i_tikai_s_step_suu = i_tikai_s_step_suu + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);    //line_step[i_egaki_dankai].setcolor(lineColor);
            }

            if (i_tikai_s_step_suu == 2) { //この段階でs_stepが[21]までうまってたら、line_step[20]とs_step[21]は共に加える折線なので、ここで処理を終えてしまう。
                //=     1+ (honsuu) +(honsuu) +  2 ){i_egaki_dankai=0; //この段階でs_stepが[21]までうまってたら、line_step[20]とs_step[21]は共に加える折線なので、ここで処理を終えてしまう。
                //例外処理としてs_step[20]とs_step[21]が平行の場合、より近いほうをs_stepが[20]とし、s_stepを[20]としてリターン（この場合はまだ処理は終われない）。
                //２つの線分が平行かどうかを判定する関数。oc.heikou_hantei(Tyokusen t1,Tyokusen t2)//0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する
                //0=平行でない、1=平行で２直線が一致しない、2=平行で２直線が一致する

                if (OritaCalc.parallel_judgement(line_step[i_drawing_stage - 1], line_step[i_drawing_stage], 0.1) != OritaCalc.ParallelJudgement.NOT_PARALLEL) {//ここは安全を見て閾値を0.1と大目にとっておこのがよさそう
                    i_drawing_stage = 0;
                    return;
                }

                i_drawing_stage = 0;

                //line_step[20]とs_step[21]の交点はoc.kouten_motome(Senbun s1,Senbun s2)で求める//２つの線分を直線とみなして交点を求める関数。線分としては交差しなくても、直線として交差している場合の交点を返す
                origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
                kousa_point.set(OritaCalc.findIntersection(line_step[2 + (honsuu) + (honsuu) + 1], line_step[2 + (honsuu) + (honsuu) + 1 + 1]));

                LineSegment add_sen = new LineSegment(kousa_point, line_step[2 + (honsuu) + (honsuu) + 1].getA());
                add_sen.setColor(lineColor);
                if (add_sen.getLength() > 0.00000001) {
                    addLineSegment(add_sen);
                }

                LineSegment add_sen2 = new LineSegment(kousa_point, line_step[2 + (honsuu) + (honsuu) + 2].getA());
                add_sen2.setColor(lineColor);
                if (add_sen.getLength() > 0.00000001) {
                    addLineSegment(add_sen2);
                }
                record();
            }

            i_drawing_stage = 0;
            return;

        }

        return;
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_17(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_17(origami_editor.graphic2d.point.Point p0) {
    }

//------


//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

//16 16 16 16 16 16    i_mouse_modeA==16　;角度系モード

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_16(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_17(p0);
    }

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_16(origami_editor.graphic2d.point.Point p0) {

        int honsuu = 0;//1つの端点周りに描く線の本数
        if (id_angle_system != 0) {
            honsuu = id_angle_system * 2 - 1;
        } else if (id_angle_system == 0) {
            honsuu = 6;
        }


        double kakudo_kei = 36.0;
        double kakudo = 0.0;
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        if ((i_drawing_stage == 0) || (i_drawing_stage == 1)) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
                if (i_drawing_stage == 0) {
                    return;
                }
            }
        }


        if (i_drawing_stage == 2) {
            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d)


            if (id_angle_system != 0) {
                d_angle_system = 180.0 / (double) id_angle_system;
            } else if (id_angle_system == 0) {
                d_angle_system = 180.0 / 4.0;
            }

            if (id_angle_system != 0) {


                LineSegment s_kiso = new LineSegment(line_step[2].getA(), line_step[1].getA());
                kakudo = 0.0;

                int i_jyun;
                i_jyun = 0;//i_jyunは線を描くとき順番に色を変えたいとき使う
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }

                    i_drawing_stage = i_drawing_stage + 1;
                    kakudo = kakudo + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakudo, 1.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }

            }

            if (id_angle_system == 0) {
                double[] jk = new double[7];
                jk[0] = 0.0;
                jk[1] = d_restricted_angle_2;
                jk[2] = d_restricted_angle_1;
                jk[3] = d_restricted_angle_3;
                jk[4] = 360.0 - d_restricted_angle_2;
                jk[5] = 360.0 - d_restricted_angle_1;
                jk[6] = 360.0 - d_restricted_angle_3;


                LineSegment s_kiso = new LineSegment(line_step[2].getA(), line_step[1].getA());
                kakudo = 0.0;


                for (int i = 1; i <= 6; i++) {

                    i_drawing_stage = i_drawing_stage + 1;
                    kakudo = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakudo, 1.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }


            }


            return;
        }


        if (i_drawing_stage == 2 + (honsuu)) {
            closest_lineSegment.set(get_moyori_step_senbun(p, 3, 2 + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                line_step[i_drawing_stage].setColor(LineColor.BLUE_2);
                return;
            }
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {
                i_drawing_stage = 0;
                return;
            }
        }


        if (i_drawing_stage == 2 + (honsuu) + 1) {

            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) >= d_decision_width) {//最寄折線が遠かった場合
                i_drawing_stage = 0;
                return;
            }

            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
                line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                //return;
            }
        }

        if (i_drawing_stage == 2 + (honsuu) + 1 + 1) {
            i_drawing_stage = 0;

            //line_step[12]とs_step[13]の交点はoc.kouten_motome(Senbun s1,Senbun s2)で求める//２つの線分を直線とみなして交点を求める関数。線分としては交差しなくても、直線として交差している場合の交点を返す
//			Ten kousa_ten =new Ten(); kousa_ten.set(oc.kouten_motome(line_step[12],line_step[13]));
            origami_editor.graphic2d.point.Point kousa_point = new origami_editor.graphic2d.point.Point();
            kousa_point.set(OritaCalc.findIntersection(line_step[2 + (honsuu) + 1], line_step[2 + (honsuu) + 1 + 1]));
            LineSegment add_sen = new LineSegment(kousa_point, line_step[2].getA(), lineColor);
            if (add_sen.getLength() > 0.00000001) {
                addLineSegment(add_sen);
                record();
            }
            return;
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_16(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_16(origami_editor.graphic2d.point.Point p0) {
    }

//------


//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA

//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV

//18 18 18 18 18 18    i_mouse_modeA==18　;角度系モード

    //マウス操作(マウスを動かしたとき)を行う関数
    public void mMoved_A_18(origami_editor.graphic2d.point.Point p0) {
        mMoved_A_17(p0);
    }

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_18(origami_editor.graphic2d.point.Point p0) {

        int honsuu = 0;//Number of lines drawn around one endpoint
        if (id_angle_system != 0) {
            honsuu = id_angle_system * 2 - 1;
        } else {
            honsuu = 6;
        }


        double kakudo_kei = 36.0;
        double kakudo = 0.0;
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        if ((i_drawing_stage == 0) || (i_drawing_stage == 1)) {
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                i_drawing_stage = i_drawing_stage + 1;
                line_step[i_drawing_stage].set(closest_point, closest_point);
                line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
                if (i_drawing_stage == 0) {
                    return;
                }
            }
        }

        if (i_drawing_stage == 2) {
            //線分abをaを中心にd度回転した線分を返す関数（元の線分は変えずに新しい線分を返す）public oc.Senbun_kaiten(Senbun s0,double d)

            if (id_angle_system != 0) {
                d_angle_system = 180.0 / (double) id_angle_system;
            } else {
                d_angle_system = 180.0 / 4.0;
            }

            if (id_angle_system != 0) {
                LineSegment s_kiso = new LineSegment(line_step[2].getA(), line_step[1].getA());
                kakudo = 0.0;

                int i_jyun;
                i_jyun = 0;//i_jyunは線を描くとき順番に色を変えたいとき使う
                for (int i = 1; i <= honsuu; i++) {
                    i_jyun = i_jyun + 1;
                    if (i_jyun == 2) {
                        i_jyun = 0;
                    }
                    i_drawing_stage = i_drawing_stage + 1;
                    kakudo = kakudo + d_angle_system;
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakudo, 100.0));
                    if (i_jyun == 0) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i_jyun == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                }
            }


            if (id_angle_system == 0) {
                double[] jk = new double[7];
                jk[0] = 0.0;
                jk[1] = d_restricted_angle_2;
                jk[2] = d_restricted_angle_1;
                jk[3] = d_restricted_angle_3;
                jk[4] = 360.0 - d_restricted_angle_2;
                jk[5] = 360.0 - d_restricted_angle_1;
                jk[6] = 360.0 - d_restricted_angle_3;

                LineSegment s_kiso = new LineSegment(line_step[2].getA(), line_step[1].getA());

                for (int i = 1; i <= 6; i++) {
                    i_drawing_stage = i_drawing_stage + 1;
                    kakudo = jk[i];
                    line_step[i_drawing_stage].set(OritaCalc.lineSegment_rotate(s_kiso, kakudo, 100.0));
                    if (i == 1) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 2) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 3) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                    if (i == 4) {
                        line_step[i_drawing_stage].setColor(LineColor.ORANGE_4);
                    }
                    if (i == 5) {
                        line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
                    }
                    if (i == 6) {
                        line_step[i_drawing_stage].setColor(LineColor.PURPLE_8);
                    }
                }
            }

            return;
        }

        if (i_drawing_stage == 2 + (honsuu)) {
            i_drawing_stage = 0;
            closest_step_lineSegment.set(get_moyori_step_senbun(p, 3, 2 + (honsuu)));
            if (OritaCalc.distance_lineSegment(p, closest_step_lineSegment) >= d_decision_width) {
                return;
            }

            if (OritaCalc.distance_lineSegment(p, closest_step_lineSegment) < d_decision_width) {
                origami_editor.graphic2d.point.Point mokuhyou_point = new origami_editor.graphic2d.point.Point();
                mokuhyou_point.set(OritaCalc.findProjection(closest_step_lineSegment, p));

                closest_lineSegment.set(getClosestLineSegment(p));
                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {//最寄折線が近い場合
                    if (OritaCalc.parallel_judgement(closest_step_lineSegment, closest_lineSegment, 0.000001) == OritaCalc.ParallelJudgement.NOT_PARALLEL) {//最寄折線が最寄step折線と平行の場合は除外
                        origami_editor.graphic2d.point.Point mokuhyou_point2 = new origami_editor.graphic2d.point.Point();
                        mokuhyou_point2.set(OritaCalc.findIntersection(closest_step_lineSegment, closest_lineSegment));
                        if (p.distance(mokuhyou_point) * 2.0 > p.distance(mokuhyou_point2)) {
                            mokuhyou_point.set(mokuhyou_point2);
                        }

                    }

                }

                LineSegment add_sen = new LineSegment();
                add_sen.set(mokuhyou_point, line_step[2].getA());
                add_sen.setColor(lineColor);
                addLineSegment(add_sen);
                record();
            }
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_18(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_18(origami_editor.graphic2d.point.Point p0) {
    }

//------


//AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA


//14 14 14 14 14 14 14 14 14    i_mouse_modeA==14　;V追加モード

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_14(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        int mts_id;
        mts_id = foldLines.closestLineSegmentSearch(p);//mts_idは点pに最も近い線分の番号	public int foldLines.mottomo_tikai_senbun_sagasi(Ten p)
        LineSegment mts = new LineSegment(foldLines.getA(mts_id), foldLines.getB(mts_id));//mtsは点pに最も近い線分

        if (OritaCalc.distance_lineSegment(p, mts) < d_decision_width) {
            //直線t上の点pの影の位置（点pと最も近い直線t上の位置）を求める。public Ten oc.kage_motome(Tyokusen t,Ten p){}
            //線分を含む直線を得る public Tyokusen oc.Senbun2Tyokusen(Senbun s){}
            origami_editor.graphic2d.point.Point pk = new origami_editor.graphic2d.point.Point();
            pk.set(OritaCalc.findProjection(OritaCalc.lineSegmentToStraightLine(mts), p));//pkは点pの（線分を含む直線上の）影

            //点paが、二点p1,p2を端点とする線分に点p1と点p2で直行する、2つの線分を含む長方形内にある場合は2を返す関数	public int oc.hakononaka(Ten p1,Ten pa,Ten p2){}

            if (OritaCalc.isInside(mts.getA(), pk, mts.getB()) == 2) {
                //線分の分割-----------------------------------------
                foldLines.lineSegment_bunkatu(mts_id, pk);  //i番目の線分(端点aとb)を点pで分割する。i番目の線分abをapに変え、線分pbを加える。
                record();
            }

        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_14(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_14(origami_editor.graphic2d.point.Point p0) {
    }

    public void v_del_all() {
        int sousuu_old = foldLines.getTotal();
        foldLines.del_V_all();
        if (sousuu_old != foldLines.getTotal()) {
            record();
        }
    }

    public void v_del_all_cc() {
        int sousuu_old = foldLines.getTotal();
        foldLines.del_V_all_cc();
        if (sousuu_old != foldLines.getTotal()) {
            record();
        }
    }

    public void all_s_step_to_orisen() {//20181014

        LineSegment add_sen = new LineSegment();
        for (int i = 1; i <= i_drawing_stage; i++) {

            if (line_step[i].getLength() > 0.00000001) {
                add_sen.set(line_step[i]);
                add_sen.setColor(lineColor);
                addLineSegment(add_sen);
            } else {

                addCircle(line_step[i].getAX(), line_step[i].getAY(), 5.0, LineColor.CYAN_3);
            }
        }
        record();
    }

//15 15 15 15 15 15 15 15 15    i_mouse_modeA==15　;V削除モード

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_15(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        //点pに最も近い線分の、点pに近い方の端点を、頂点とした場合、何本の線分が出ているか（頂点とr以内に端点がある線分の数）	public int tyouten_syuui_sennsuu(Ten p) {

        foldLines.del_V(p, d_decision_width, 0.000001);
        record();
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_15(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_15(origami_editor.graphic2d.point.Point p0) {
    }


//------

//41 41 41 41 41 41 41 41    i_mouse_modeA==41　;V削除モード(2つの折線の色が違った場合カラーチェンジして、点削除する。黒赤は赤赤、黒青は青青、青赤は黒にする)

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_41(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));

        //点pに最も近い線分の、点pに近い方の端点を、頂点とした場合、何本の線分が出ているか（頂点とr以内に端点がある線分の数）	public int tyouten_syuui_sennsuu(Ten p) {

        foldLines.del_V_cc(p, d_decision_width, 0.000001);

        record();
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_41(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_41(origami_editor.graphic2d.point.Point p0) {
    }


//------

    //-------------------------
//23 23 23 23 23
    //マウス操作(i_mouse_modeA==23 "->M" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_23(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==23でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_23(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==23 でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_23(origami_editor.graphic2d.point.Point p0) {//ここの処理の終わりに fix2(0.001,0.5);　をするのは、元から折線だったものと、補助線から変換した折線との組合せで頻発するT字型不接続を修正するため
        i_drawing_stage = 0;

        if (p19_1.distance(p0) > 0.000001) {//現状では赤を赤に変えたときもUNDO用に記録されてしまう20161218
            if (M_nisuru(p19_1, p0) != 0) {
                fix2(0.001, 0.5);
                record();
            }
        }
        if (p19_1.distance(p0) <= 0.000001) {//現状では赤を赤に変えたときもUNDO用に記録されてしまう20161218
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double closestLineSegmentDistance(Ten p)
                foldLines.setColor(foldLines.closestLineSegmentSearch(p), LineColor.RED_1);
                fix2(0.001, 0.5);
                record();
            }
        }

    }

    //--------------------
    public int M_nisuru(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.M_nisuru(p_a, p_b, p_c, p_d);
    }

    //---------------------
//24 24 24 24 24
    //マウス操作(i_mouse_modeA==24 "->V" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_24(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==24でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_24(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==24 でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_24(origami_editor.graphic2d.point.Point p0) {//ここの処理の終わりに fix2(0.001,0.5);　をするのは、元から折線だったものと、補助線から変換した折線との組合せで頻発するT字型不接続を修正するため
        i_drawing_stage = 0;

        if (p19_1.distance(p0) > 0.000001) {
            if (V_nisuru(p19_1, p0) != 0) {
                fix2(0.001, 0.5);
                record();
            }
        }
        if (p19_1.distance(p0) <= 0.000001) {
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                foldLines.setColor(foldLines.closestLineSegmentSearch(p), LineColor.BLUE_2);
                fix2(0.001, 0.5);
                record();
            }
        }
    }

    public int V_nisuru(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.V_nisuru(p_a, p_b, p_c, p_d);
    }

    //---------------------
//25 25 25 25 25
    //マウス操作(i_mouse_modeA==25 "->E" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_25(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==25でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_25(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==25 でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_25(origami_editor.graphic2d.point.Point p0) {//ここの処理の終わりに fix2(0.001,0.5);　をするのは、元から折線だったものと、補助線から変換した折線との組合せで頻発するT字型不接続を修正するため
        i_drawing_stage = 0;

        if (p19_1.distance(p0) > 0.000001) {
            if (E_nisuru(p19_1, p0) != 0) {
                fix2(0.001, 0.5);
                record();
            }
        }

        if (p19_1.distance(p0) <= 0.000001) {
            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double mottomo_tikai_senbun_kyori(Ten p)
                foldLines.setColor(foldLines.closestLineSegmentSearch(p), LineColor.BLACK_0);
                fix2(0.001, 0.5);
                record();
            }
        }
    }

    public int E_nisuru(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.E_nisuru(p_a, p_b, p_c, p_d);
    }

    //60 60 60 60 60
    //マウス操作(i_mouse_modeA==60 "->HK" でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_60(origami_editor.graphic2d.point.Point p0) {
        mPressed_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==60でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_60(origami_editor.graphic2d.point.Point p0) {
        mDragged_A_box_select(p0);
    }

    //マウス操作(i_mouse_modeA==60 でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_60(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 0;

        if (p19_1.distance(p0) > 0.000001) {
            if (HK_nisuru(p19_1, p0) != 0) {
                record();
            }//この関数は不完全なのでまだ未公開20171126
        }

        if (p19_1.distance(p0) <= 0.000001) {
            p.set(camera.TV2object(p0));
            if (foldLines.closestLineSegmentDistance(p) < d_decision_width) {//点pに最も近い線分の番号での、その距離を返す	public double closestLineSegmentDistance(Ten p)
                if (foldLines.getColor(foldLines.closestLineSegmentSearchReversedOrder(p)).getNumber() < 3) {
                    LineSegment add_sen = new LineSegment();
                    add_sen.set(foldLines.get(foldLines.closestLineSegmentSearchReversedOrder(p)));
                    add_sen.setColor(LineColor.CYAN_3);

                    foldLines.deleteLineSegment_vertex(foldLines.closestLineSegmentSearchReversedOrder(p));
                    addLineSegment(add_sen);

                    circle_organize();
                    record();
                }
            }
        }


    }

    //--------------------
    public int HK_nisuru(origami_editor.graphic2d.point.Point p0a, origami_editor.graphic2d.point.Point p0b) {
        origami_editor.graphic2d.point.Point p0_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p0_d = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_a = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_b = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_c = new origami_editor.graphic2d.point.Point();
        origami_editor.graphic2d.point.Point p_d = new origami_editor.graphic2d.point.Point();
        p0_a.set(p0a.getX(), p0a.getY());
        p0_b.set(p0a.getX(), p0b.getY());
        p0_c.set(p0b.getX(), p0b.getY());
        p0_d.set(p0b.getX(), p0a.getY());
        p_a.set(camera.TV2object(p0_a));
        p_b.set(camera.TV2object(p0_b));
        p_c.set(camera.TV2object(p0_c));
        p_d.set(camera.TV2object(p0_d));
        return foldLines.HK_nisuru(p_a, p_b, p_c, p_d);
    }

    public LineSegment get_s_step(int i) {
        return line_step[i];
    }


//26 26 26 26    i_mouse_modeA==26　;背景setモード。

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_26(origami_editor.graphic2d.point.Point p0) {


        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        if (i_drawing_stage == 3) {
            i_drawing_stage = 4;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                p.set(closest_point);
            }
            line_step[4].set(p, p);
            line_step[4].setColor(LineColor.fromNumber(i_drawing_stage));
        }

        if (i_drawing_stage == 2) {
            i_drawing_stage = 3;
            closest_point.set(getClosestPoint(p));
            if (p.distance(closest_point) < d_decision_width) {
                p.set(closest_point);
            }
            line_step[3].set(p, p);
            line_step[3].setColor(LineColor.fromNumber(i_drawing_stage));
        }

        if (i_drawing_stage == 1) {
            i_drawing_stage = 2;
            line_step[2].set(p, p);
            line_step[2].setColor(LineColor.fromNumber(i_drawing_stage));
        }

        if (i_drawing_stage == 0) {
            i_drawing_stage = 1;
            line_step[1].set(p, p);
            line_step[1].setColor(LineColor.fromNumber(i_drawing_stage));
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_26(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public int mReleased_A_26(origami_editor.graphic2d.point.Point p0) {
        return i_drawing_stage;
    }

//------


//42 42 42 42 42 42 42 42 42 42 42 42 42 42 42　ここから

    //マウス操作(i_mouse_modeA==42 円入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_42(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;
        i_circle_drawing_stage = 1;

        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
        }
        line_step[1].set(p, closest_point);
        line_step[1].setColor(LineColor.CYAN_3);
        circle_step[1].set(closest_point.getX(), closest_point.getY(), 0.0);
        circle_step[1].setColor(LineColor.CYAN_3);
    }

    //マウス操作(i_mouse_modeA==42 円入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_42(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        circle_step[1].setR(OritaCalc.distance(line_step[1].getA(), line_step[1].getB()));
    }

    //マウス操作(i_mouse_modeA==42 円入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_42(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {
                    addCircle(line_step[1].getBX(), line_step[1].getBY(), line_step[1].getLength(), LineColor.CYAN_3);
                    record();
                }
            }
        }
    }

//42 42 42 42 42 42 42 42 42 42 42 42 42 42 42  ここまで


//47 47 47 47 47 47 47 47 47 47 47 47 47 47 47　ここから

    //マウス操作(i_mouse_modeA==47 円入力(フリー　)　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_47(origami_editor.graphic2d.point.Point p0) {
        i_drawing_stage = 1;
        i_circle_drawing_stage = 1;

        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) > d_decision_width) {
            line_step[1].set(p, p);
            line_step[1].setColor(LineColor.CYAN_3);
            circle_step[1].set(p.getX(), p.getY(), 0.0);
            circle_step[1].setColor(LineColor.CYAN_3);
        } else {
            line_step[1].set(p, closest_point);
            line_step[1].setColor(LineColor.CYAN_3);
            circle_step[1].set(closest_point.getX(), closest_point.getY(), 0.0);
            circle_step[1].setColor(LineColor.CYAN_3);
        }
    }

    //マウス操作(i_mouse_modeA==47 円入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_47(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        line_step[1].setA(p);
        circle_step[1].setR(OritaCalc.distance(line_step[1].getA(), line_step[1].getB()));
    }

    //マウス操作(i_mouse_modeA==47 円入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_47(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 1) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));

            if (p.distance(closest_point) <= d_decision_width) {
                line_step[1].setA(closest_point);
            } else {
                line_step[1].setA(p);
            }

            if (line_step[1].getLength() > 0.00000001) {
                addCircle(line_step[1].getBX(), line_step[1].getBY(), line_step[1].getLength(), LineColor.CYAN_3);
                record();
            }
        }
    }

//47 47 47 47 47 47 47 47 47 47 47 47 47 47 47  ここまで


//44 44 44 44 44 44 44 44 44 44 44 44 44 44 44　ここから

    //マウス操作(i_mouse_modeA==44 円 分離入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_44(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));

        if (i_drawing_stage == 0) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
            if (p.distance(closest_point) > d_decision_width) {
                return;
            }

            i_drawing_stage = 1;
            i_circle_drawing_stage = 0;
            line_step[1].set(closest_point, closest_point);
            line_step[1].setColor(LineColor.CYAN_3);
            return;
        }

        if (i_drawing_stage == 1) {
            i_drawing_stage = 1;
            i_circle_drawing_stage = 0;
            if (p.distance(closest_point) > d_decision_width) {
                return;
            }

            i_drawing_stage = 2;
            i_circle_drawing_stage = 1;
            line_step[2].set(p, closest_point);
            line_step[2].setColor(LineColor.CYAN_3);
            circle_step[1].set(line_step[1].getA(), 0.0, LineColor.CYAN_3);
            return;
        }


    }

    //マウス操作(i_mouse_modeA==44 円 分離入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_44(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        if (i_drawing_stage == 2) {
            i_drawing_stage = 2;
            i_circle_drawing_stage = 1;
            line_step[2].setA(p);
            circle_step[1].setR(line_step[2].getLength());
        }
    }

    //マウス操作(i_mouse_modeA==44 円 分離入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_44(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 2) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[2].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[2].getLength() > 0.00000001) {
                    addLineSegment(line_step[2]);
                    addCircle(line_step[1].getA(), line_step[2].getLength(), LineColor.CYAN_3);
                    record();
                }
            }
        }
    }

//44 44 44 44 44 44 44 44 44 44 44 44 44 44 44  ここまで


//48 48 48 48 48 48 48 48 48 48 48 48 48 48 48　ここから

    //マウス操作(i_mouse_modeA==48 同心円　線分入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_48(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_circumference.set(get_moyori_ensyuu(p));
        closest_point.set(getClosestPoint(p));

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 0)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            circle_step[1].set(closest_circumference);
            circle_step[1].setColor(LineColor.GREEN_6);
            return;
        }

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 1)) {
            if (p.distance(closest_point) > d_decision_width) {
                return;
            }

            i_drawing_stage = 1;
            i_circle_drawing_stage = 2;
            line_step[1].set(p, closest_point);
            line_step[1].setColor(LineColor.CYAN_3);
            circle_step[2].set(circle_step[1]);
            circle_step[2].setColor(LineColor.CYAN_3);
            return;
        }
    }

    //マウス操作(i_mouse_modeA==48 同心円　線分入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_48(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        if ((i_drawing_stage == 1) && (i_circle_drawing_stage == 2)) {
            line_step[1].setA(p);
            circle_step[2].setR(circle_step[1].getRadius() + line_step[1].getLength());
        }
    }

    //マウス操作(i_mouse_modeA==48 同心円　線分入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_48(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage == 1) && (i_circle_drawing_stage == 2)) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            //Ten p =new Ten();
            p.set(camera.TV2object(p0));
            closest_point.set(getClosestPoint(p));
            line_step[1].setA(closest_point);
            if (p.distance(closest_point) <= d_decision_width) {
                if (line_step[1].getLength() > 0.00000001) {
                    addLineSegment(line_step[1]);
                    circle_step[2].setR(circle_step[1].getRadius() + line_step[1].getLength());
                    addCircle(circle_step[2]);
                    record();
                }
            }
        }
    }

//48 48 48 48 48 48 48 48 48 48 48 48 48 48 48  ここまで

//49 49 49 49 49 49 49 49 49 49 49 49 49 49 49　ここから

    //マウス操作(i_mouse_modeA==49 同心円　同心円入力　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_49(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_circumference.set(get_moyori_ensyuu(p));
        closest_point.set(getClosestPoint(p));

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 0)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            circle_step[1].set(closest_circumference);
            circle_step[1].setColor(LineColor.GREEN_6);
            return;
        }

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 1)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 2;
            circle_step[2].set(closest_circumference);
            circle_step[2].setColor(LineColor.PURPLE_8);
            return;
        }

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 2)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 3;
            circle_step[3].set(closest_circumference);
            circle_step[3].setColor(LineColor.PURPLE_8);
            return;
        }
    }

    //マウス操作(i_mouse_modeA==49 同心円　同心円入力　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_49(origami_editor.graphic2d.point.Point p0) {

    }

    //マウス操作(i_mouse_modeA==49 同心円　同心円入力　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_49(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 3)) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
            double add_r = circle_step[3].getRadius() - circle_step[2].getRadius();
            if (Math.abs(add_r) > 0.00000001) {
                double new_r = add_r + circle_step[1].getRadius();

                if (new_r > 0.00000001) {
                    circle_step[1].setR(new_r);
                    circle_step[1].setColor(LineColor.CYAN_3);
                    addCircle(circle_step[1]);
                    record();
                }
            }
        }
    }

//49 49 49 49 49 49 49 49 49 49 49 49 49 49 49  ここまで

//51 51 51 51 51 51 51 51 51 51 51 51 51 51 51　ここから

    //マウス操作(i_mouse_modeA==51 平行線　幅指定入力モード　でボタンを押したとき)時の作業----------------------------------------------------
    public void mPressed_A_51(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 0)) {
            closest_lineSegment.set(getClosestLineSegment(p));
            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
                i_drawing_stage = 1;
                i_circle_drawing_stage = 0;
                line_step[1].set(closest_lineSegment);
                line_step[1].setColor(LineColor.GREEN_6);
            }
            return;
        }

        if ((i_drawing_stage == 1) && (i_circle_drawing_stage == 0)) {
            if (p.distance(closest_point) > d_decision_width) {
                return;
            }
            i_drawing_stage = 4;
            i_circle_drawing_stage = 0;
            line_step[2].set(p, closest_point);
            line_step[2].setColor(LineColor.CYAN_3);
            line_step[3].set(line_step[1]);
            line_step[3].setColor(LineColor.PURPLE_8);
            line_step[4].set(line_step[1]);
            line_step[4].setColor(LineColor.PURPLE_8);
            return;
        }


        if ((i_drawing_stage == 4) && (i_circle_drawing_stage == 0)) {
            i_drawing_stage = 3;
            i_circle_drawing_stage = 0;
            closest_step_lineSegment.set(get_moyori_step_senbun(p, 3, 4));

            line_step[3].set(closest_step_lineSegment);
            return;
        }


    }

    //マウス操作(i_mouse_modeA==51 平行線　幅指定入力モード　でドラッグしたとき)を行う関数----------------------------------------------------
    public void mDragged_A_51(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        if ((i_drawing_stage == 4) && (i_circle_drawing_stage == 0)) {
            line_step[2].setA(p);
            line_step[3].set(OritaCalc.moveParallel(line_step[1], line_step[2].getLength()));
            line_step[3].setColor(LineColor.PURPLE_8);
            line_step[4].set(OritaCalc.moveParallel(line_step[1], -line_step[2].getLength()));
            line_step[4].setColor(LineColor.PURPLE_8);
        }
    }

    //マウス操作(i_mouse_modeA==51 平行線　幅指定入力モード　でボタンを離したとき)を行う関数----------------------------------------------------
    public void mReleased_A_51(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));

        if ((i_drawing_stage == 4) && (i_circle_drawing_stage == 0)) {
            if (p.distance(closest_point) >= d_decision_width) {
                i_drawing_stage = 1;
                i_circle_drawing_stage = 0;
                return;
            }

            line_step[2].setA(closest_point);

            if (line_step[2].getLength() < 0.00000001) {
                i_drawing_stage = 1;
                i_circle_drawing_stage = 0;
                return;
            }
            line_step[3].set(OritaCalc.moveParallel(line_step[1], line_step[2].getLength()));
            line_step[3].setColor(LineColor.PURPLE_8);
            line_step[4].set(OritaCalc.moveParallel(line_step[1], -line_step[2].getLength()));
            line_step[4].setColor(LineColor.PURPLE_8);
        }


        if ((i_drawing_stage == 3) && (i_circle_drawing_stage == 0)) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            line_step[3].setColor(lineColor);
            addLineSegment(line_step[3]);
            record();

            return;
        }


    }

//51 51 51 51 51 51 51 51 51 51 51 51 51 51 51  ここまで

//45 45 45 45 45 45 45 45 45   i_mouse_modeA==45　;2円の共通接線入力モード。

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_45(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_circumference.set(get_moyori_ensyuu(p));

        if (i_circle_drawing_stage == 0) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            circle_step[1].set(closest_circumference);
            circle_step[1].setColor(LineColor.GREEN_6);
            return;
        }

        if (i_circle_drawing_stage == 1) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 2;
            circle_step[2].set(closest_circumference);
            circle_step[2].setColor(LineColor.GREEN_6);
            return;
        }

        if (i_drawing_stage > 1) {//			i_egaki_dankai=0;i_circle_drawing_stage=1;
            closest_step_lineSegment.set(get_moyori_step_senbun(p, 1, i_drawing_stage));

            if (OritaCalc.distance_lineSegment(p, closest_step_lineSegment) > d_decision_width) {
                return;
            }
            line_step[1].set(closest_step_lineSegment);
            i_drawing_stage = 1;
            i_circle_drawing_stage = 2;

            return;
        }


    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_45(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_45(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 2)) {
            origami_editor.graphic2d.point.Point c1 = new origami_editor.graphic2d.point.Point();
            c1.set(circle_step[1].getCenter());
            origami_editor.graphic2d.point.Point c2 = new origami_editor.graphic2d.point.Point();
            c2.set(circle_step[2].getCenter());

            double x1 = circle_step[1].getX();
            double y1 = circle_step[1].getY();
            double r1 = circle_step[1].getRadius();
            double x2 = circle_step[2].getX();
            double y2 = circle_step[2].getY();
            double r2 = circle_step[2].getRadius();
            //0,0,r,        xp,yp,R
            double xp = x2 - x1;
            double yp = y2 - y1;

            if (c1.distance(c2) < 0.000001) {
                i_drawing_stage = 0;
                i_circle_drawing_stage = 0;
                return;
            }//接線0本の場合

            if ((xp * xp + yp * yp) < (r1 - r2) * (r1 - r2)) {
                i_drawing_stage = 0;
                i_circle_drawing_stage = 0;
                return;
            }//接線0本の場合

            if (Math.abs((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2)) < 0.0000001) {//外接線1本の場合
                origami_editor.graphic2d.point.Point kouten = new origami_editor.graphic2d.point.Point();
                kouten.set(OritaCalc.internalDivisionRatio(c1, c2, -r1, r2));
                StraightLine ty = new StraightLine(c1, kouten);
                ty.orthogonalize(kouten);
                line_step[1].set(OritaCalc.circle_to_straightLine_no_intersect_wo_connect_LineSegment(new Circle(kouten, (r1 + r2) / 2.0, LineColor.BLACK_0), ty));

                i_drawing_stage = 1;
                i_circle_drawing_stage = 2;
            }

            if (((r1 - r2) * (r1 - r2) < (xp * xp + yp * yp)) && ((xp * xp + yp * yp) < (r1 + r2) * (r1 + r2))) {//外接線2本の場合
                double xq1 = r1 * (xp * (r1 - r2) + yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq1 = r1 * (yp * (r1 - r2) - xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double xq2 = r1 * (xp * (r1 - r2) - yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq2 = r1 * (yp * (r1 - r2) + xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線

                double xr1 = xq1 + x1;
                double yr1 = yq1 + y1;
                double xr2 = xq2 + x1;
                double yr2 = yq2 + y1;

                StraightLine t1 = new StraightLine(x1, y1, xr1, yr1);
                t1.orthogonalize(new origami_editor.graphic2d.point.Point(xr1, yr1));
                StraightLine t2 = new StraightLine(x1, y1, xr2, yr2);
                t2.orthogonalize(new origami_editor.graphic2d.point.Point(xr2, yr2));

                line_step[1].set(new origami_editor.graphic2d.point.Point(xr1, yr1), OritaCalc.findProjection(t1, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[1].setColor(LineColor.PURPLE_8);
                line_step[2].set(new origami_editor.graphic2d.point.Point(xr2, yr2), OritaCalc.findProjection(t2, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[2].setColor(LineColor.PURPLE_8);

                i_drawing_stage = 2;
                i_circle_drawing_stage = 2;

            }

            if (Math.abs((xp * xp + yp * yp) - (r1 + r2) * (r1 + r2)) < 0.0000001) {//外接線2本と内接線1本の場合
                double xq1 = r1 * (xp * (r1 - r2) + yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq1 = r1 * (yp * (r1 - r2) - xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double xq2 = r1 * (xp * (r1 - r2) - yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq2 = r1 * (yp * (r1 - r2) + xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線

                double xr1 = xq1 + x1;
                double yr1 = yq1 + y1;
                double xr2 = xq2 + x1;
                double yr2 = yq2 + y1;

                StraightLine t1 = new StraightLine(x1, y1, xr1, yr1);
                t1.orthogonalize(new origami_editor.graphic2d.point.Point(xr1, yr1));
                StraightLine t2 = new StraightLine(x1, y1, xr2, yr2);
                t2.orthogonalize(new origami_editor.graphic2d.point.Point(xr2, yr2));

                line_step[1].set(new origami_editor.graphic2d.point.Point(xr1, yr1), OritaCalc.findProjection(t1, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[1].setColor(LineColor.PURPLE_8);
                line_step[2].set(new origami_editor.graphic2d.point.Point(xr2, yr2), OritaCalc.findProjection(t2, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[2].setColor(LineColor.PURPLE_8);

                // -----------------------

                origami_editor.graphic2d.point.Point kouten = new origami_editor.graphic2d.point.Point();
                kouten.set(OritaCalc.internalDivisionRatio(c1, c2, r1, r2));
                StraightLine ty = new StraightLine(c1, kouten);
                ty.orthogonalize(kouten);
                line_step[3].set(OritaCalc.circle_to_straightLine_no_intersect_wo_connect_LineSegment(new Circle(kouten, (r1 + r2) / 2.0, LineColor.BLACK_0), ty));
                line_step[3].setColor(LineColor.PURPLE_8);
                // -----------------------

                i_drawing_stage = 3;
                i_circle_drawing_stage = 2;
            }

            if ((r1 + r2) * (r1 + r2) < (xp * xp + yp * yp)) {//外接線2本と内接線2本の場合
                //             ---------------------------------------------------------------
                //                                     -------------------------------------
                //                 -------               -------------   -------   -------       -------------
                double xq1 = r1 * (xp * (r1 - r2) + yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq1 = r1 * (yp * (r1 - r2) - xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double xq2 = r1 * (xp * (r1 - r2) - yp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double yq2 = r1 * (yp * (r1 - r2) + xp * Math.sqrt((xp * xp + yp * yp) - (r1 - r2) * (r1 - r2))) / (xp * xp + yp * yp);//共通外接線
                double xq3 = r1 * (xp * (r1 + r2) + yp * Math.sqrt((xp * xp + yp * yp) - (r1 + r2) * (r1 + r2))) / (xp * xp + yp * yp);//共通内接線
                double yq3 = r1 * (yp * (r1 + r2) - xp * Math.sqrt((xp * xp + yp * yp) - (r1 + r2) * (r1 + r2))) / (xp * xp + yp * yp);//共通内接線
                double xq4 = r1 * (xp * (r1 + r2) - yp * Math.sqrt((xp * xp + yp * yp) - (r1 + r2) * (r1 + r2))) / (xp * xp + yp * yp);//共通内接線
                double yq4 = r1 * (yp * (r1 + r2) + xp * Math.sqrt((xp * xp + yp * yp) - (r1 + r2) * (r1 + r2))) / (xp * xp + yp * yp);//共通内接線


                double xr1 = xq1 + x1;
                double yr1 = yq1 + y1;
                double xr2 = xq2 + x1;
                double yr2 = yq2 + y1;
                double xr3 = xq3 + x1;
                double yr3 = yq3 + y1;
                double xr4 = xq4 + x1;
                double yr4 = yq4 + y1;

                StraightLine t1 = new StraightLine(x1, y1, xr1, yr1);
                t1.orthogonalize(new origami_editor.graphic2d.point.Point(xr1, yr1));
                StraightLine t2 = new StraightLine(x1, y1, xr2, yr2);
                t2.orthogonalize(new origami_editor.graphic2d.point.Point(xr2, yr2));
                StraightLine t3 = new StraightLine(x1, y1, xr3, yr3);
                t3.orthogonalize(new origami_editor.graphic2d.point.Point(xr3, yr3));
                StraightLine t4 = new StraightLine(x1, y1, xr4, yr4);
                t4.orthogonalize(new origami_editor.graphic2d.point.Point(xr4, yr4));

                line_step[1].set(new origami_editor.graphic2d.point.Point(xr1, yr1), OritaCalc.findProjection(t1, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[1].setColor(LineColor.PURPLE_8);
                line_step[2].set(new origami_editor.graphic2d.point.Point(xr2, yr2), OritaCalc.findProjection(t2, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[2].setColor(LineColor.PURPLE_8);
                line_step[3].set(new origami_editor.graphic2d.point.Point(xr3, yr3), OritaCalc.findProjection(t3, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[3].setColor(LineColor.PURPLE_8);
                line_step[4].set(new origami_editor.graphic2d.point.Point(xr4, yr4), OritaCalc.findProjection(t4, new origami_editor.graphic2d.point.Point(x2, y2)));
                line_step[4].setColor(LineColor.PURPLE_8);

                i_drawing_stage = 4;
                i_circle_drawing_stage = 2;
            }
        }

        if (i_drawing_stage == 1) {

            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;

            line_step[1].setColor(lineColor);
            addLineSegment(line_step[1]);
            record();

            return;
        }
    }

//45 45 45 45 45 45 45 45 45  ここまで  ------


//50 50 50 50 50 50 50 50 50   i_mouse_modeA==50　;2円に幅同じで接する同心円を加える。

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_50(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_circumference.set(get_moyori_ensyuu(p));
        closest_point.set(getClosestPoint(p));

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 0)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            circle_step[1].set(closest_circumference);
            circle_step[1].setColor(LineColor.GREEN_6);
            return;
        }

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 1)) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 2;
            circle_step[2].set(closest_circumference);
            circle_step[2].setColor(LineColor.GREEN_6);
            return;
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_50(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_50(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 2)) {
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
            double add_r = (OritaCalc.distance(circle_step[1].getCenter(), circle_step[2].getCenter()) - circle_step[1].getRadius() - circle_step[2].getRadius()) * 0.5;

            if (Math.abs(add_r) > 0.00000001) {
                double new_r1 = add_r + circle_step[1].getRadius();
                double new_r2 = add_r + circle_step[2].getRadius();

                if ((new_r1 > 0.00000001) && (new_r2 > 0.00000001)) {
                    circle_step[1].setR(new_r1);
                    circle_step[1].setColor(LineColor.CYAN_3);
                    addCircle(circle_step[1]);
                    circle_step[2].setR(new_r2);
                    circle_step[2].setColor(LineColor.CYAN_3);
                    addCircle(circle_step[2]);
                    record();
                }
            }
        }

    }

//50 50 50 50 50 50 50 50 50  ここまで  ------


//46 46 46 46 46 46 46 46 46   i_mouse_modeA==46　;反転入力モード。

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_46(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));

        closest_circumference.set(get_moyori_ensyuu(p));

        if (i_drawing_stage + i_circle_drawing_stage == 0) {
            closest_lineSegment.set(getClosestLineSegment(p));

            if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < OritaCalc.distance_circumference(p, closest_circumference)) {//線分の方が円周より近い
                i_drawing_stage = 0;
                i_circle_drawing_stage = 0;
                if (OritaCalc.distance_lineSegment(p, closest_lineSegment) > d_decision_width) {
                    return;
                }
                i_drawing_stage = 1;
                i_circle_drawing_stage = 0;
                line_step[1].set(closest_lineSegment);
                line_step[1].setColor(LineColor.GREEN_6);
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }

            i_drawing_stage = 0;
            i_circle_drawing_stage = 1;
            circle_step[1].set(closest_circumference);
            circle_step[1].setColor(LineColor.GREEN_6);
            return;
        }

        if (i_drawing_stage + i_circle_drawing_stage == 1) {
            if (OritaCalc.distance_circumference(p, closest_circumference) > d_decision_width) {
                return;
            }
            i_circle_drawing_stage = i_circle_drawing_stage + 1;
            circle_step[i_circle_drawing_stage].set(closest_circumference);
            circle_step[i_circle_drawing_stage].setColor(LineColor.RED_1);
            return;
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_46(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_46(origami_editor.graphic2d.point.Point p0) {
        if ((i_drawing_stage == 1) && (i_circle_drawing_stage == 1)) {

            add_hanten(line_step[1], circle_step[1]);
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
        }

        if ((i_drawing_stage == 0) && (i_circle_drawing_stage == 2)) {
            add_hanten(circle_step[1], circle_step[2]);
            i_drawing_stage = 0;
            i_circle_drawing_stage = 0;
        }
    }

//46 46 46 46 46 46 46 46 46  ここまで  ------


//43 43 43 43 43 43 43 43 43   i_mouse_modeA==43　;円3点入力モード。

    //マウス操作(ボタンを押したとき)時の作業
    public void mPressed_A_43(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
        }
    }

    //マウス操作(ドラッグしたとき)を行う関数
    public void mDragged_A_43(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(ボタンを離したとき)を行う関数
    public void mReleased_A_43(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;

            LineSegment sen1 = new LineSegment(line_step[1].getA(), line_step[2].getA());
            if (sen1.getLength() < 0.00000001) {
                return;
            }
            LineSegment sen2 = new LineSegment(line_step[2].getA(), line_step[3].getA());
            if (sen2.getLength() < 0.00000001) {
                return;
            }
            LineSegment sen3 = new LineSegment(line_step[3].getA(), line_step[1].getA());
            if (sen3.getLength() < 0.00000001) {
                return;
            }

            if (Math.abs(OritaCalc.angle(sen1, sen2) - 0.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen1, sen2) - 180.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen1, sen2) - 360.0) < 0.000001) {
                return;
            }

            if (Math.abs(OritaCalc.angle(sen2, sen3) - 0.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen2, sen3) - 180.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen2, sen3) - 360.0) < 0.000001) {
                return;
            }

            if (Math.abs(OritaCalc.angle(sen3, sen1) - 0.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen3, sen1) - 180.0) < 0.000001) {
                return;
            }
            if (Math.abs(OritaCalc.angle(sen3, sen1) - 360.0) < 0.000001) {
                return;
            }

            StraightLine t1 = new StraightLine(sen1);
            t1.orthogonalize(OritaCalc.internalDivisionRatio(sen1.getA(), sen1.getB(), 1.0, 1.0));
            StraightLine t2 = new StraightLine(sen2);
            t2.orthogonalize(OritaCalc.internalDivisionRatio(sen2.getA(), sen2.getB(), 1.0, 1.0));
            addCircle(OritaCalc.findIntersection(t1, t2), OritaCalc.distance(line_step[1].getA(), OritaCalc.findIntersection(t1, t2)), LineColor.CYAN_3);
            record();
        }
    }

    //マウス操作(i_mouse_modeA==10001　でボタンを押したとき)時の作業
    public void mPressed_A_10001(origami_editor.graphic2d.point.Point p0) {
        p.set(camera.TV2object(p0));
        closest_point.set(getClosestPoint(p));
        if (p.distance(closest_point) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_point, closest_point);
            line_step[i_drawing_stage].setColor(LineColor.fromNumber(i_drawing_stage));
        }
    }

    //マウス操作(i_mouse_modeA==10001　でドラッグしたとき)を行う関数
    public void mDragged_A_10001(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(i_mouse_modeA==10001　でボタンを離したとき)を行う関数
    public void mReleased_A_10001(origami_editor.graphic2d.point.Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
        }
    }

//------
//10002

    //マウス操作(i_mouse_modeA==10002　でボタンを押したとき)時の作業
    public void mPressed_A_10002(origami_editor.graphic2d.point.Point p0) {
        //Ten p =new Ten();
        p.set(camera.TV2object(p0));
        closest_lineSegment.set(getClosestLineSegment(p));
        if (OritaCalc.distance_lineSegment(p, closest_lineSegment) < d_decision_width) {
            i_drawing_stage = i_drawing_stage + 1;
            line_step[i_drawing_stage].set(closest_lineSegment);//line_step[i_egaki_dankai].setcolor(i_egaki_dankai);
            line_step[i_drawing_stage].setColor(LineColor.GREEN_6);
        }
    }

    //マウス操作(i_mouse_modeA==10002　でドラッグしたとき)を行う関数
    public void mDragged_A_10002(origami_editor.graphic2d.point.Point p0) {
    }

    //マウス操作(i_mouse_modeA==10002　でボタンを離したとき)を行う関数
    public void mReleased_A_10002(Point p0) {
        if (i_drawing_stage == 3) {
            i_drawing_stage = 0;
        }
    }

    public Grid.State getBaseState() {
        return grid.getBaseState();
    }

    public void setBaseState(Grid.State i) {
        grid.setBaseState(i);
    }

    public void setFoldLineDividingNumber(int i) {
        foldLineDividingNumber = i;
        if (foldLineDividingNumber < 1) {
            foldLineDividingNumber = 1;
        }
    }

    public void set_d_internalDivisionRatio_st(double ds, double dt) {
        d_internalDivisionRatio_s = ds;
        d_internalDivisionRatio_t = dt;
    }

    public void set_d_restricted_angle(double d_1, double d_2, double d_3) {
        d_restricted_angle_1 = d_1;
        d_restricted_angle_2 = d_2;
        d_restricted_angle_3 = d_3;
    }

    public void setNumPolygonCorners(int i) {
        numPolygonCorners = i;
        if (numPolygonCorners < 3) {
            foldLineDividingNumber = 3;
        }
    }

    public void setFoldLineAdditional(FoldLineAdditionalInputMode i) {
        i_foldLine_additional_old = i_foldLine_additional;
        i_foldLine_additional = i;
    }

    public void modosi_foldLineAdditional() {
        i_foldLine_additional = i_foldLine_additional_old;
    }

    public void check1(double r_hitosii, double parallel_decision) {
        foldLines.check1(r_hitosii, parallel_decision);
    }//In foldLines, check and set the funny fold line to the selected state.

    public void fix1(double r_hitosii, double heikou_hantei) {
        while (foldLines.fix1(r_hitosii, heikou_hantei)) {
        }
        //foldLines.addsenbun  delsenbunを実施しているところでcheckを実施
        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

    }

    public void set_i_check1(boolean i) {
        check1 = i;
    }

    public void check2(double r_hitosii, double heikou_hantei) {
        foldLines.check2(r_hitosii, heikou_hantei);
    }

    public void fix2(double r_hitosii, double heikou_hantei) {
        while (foldLines.fix2(r_hitosii, heikou_hantei) == 1) {
        }
        //foldLines.addsenbun  delsenbunを実施しているところでcheckを実施
        if (check1) {
            check1(0.001, 0.5);
        }
        if (check2) {
            check2(0.01, 0.5);
        }
        if (check3) {
            check3(0.0001);
        }
        if (check4) {
            check4(0.0001);
        }

    }

    public void setCheck2(boolean i) {
        check2 = i;
    }

    public void check3(double r) {
        foldLines.check3(r);
    }

    public void check4(double r) {
        app.check4(r);
    }

    public void ap_check4(double r) {
        foldLines.check4(r);
    }

    public void setCheck3(boolean i) {
        check3 = i;
    }

    public void setCheck4(boolean i) {
        check4 = i;
    }

    public void ck4_color_sage() {
        i_ck4_color_toukado = i_ck4_color_toukado - i_ck4_color_toukado_sabun;
        if (i_ck4_color_toukado < 50) {
            i_ck4_color_toukado = i_ck4_color_toukado + i_ck4_color_toukado_sabun;
        }
    }

    public void ck4_color_age() {
        i_ck4_color_toukado = i_ck4_color_toukado + i_ck4_color_toukado_sabun;
        if (i_ck4_color_toukado > 250) {
            i_ck4_color_toukado = i_ck4_color_toukado - i_ck4_color_toukado_sabun;
        }
    }

    public void h_setcolor(LineColor i) {
        auxLineColor = i;
    }

    public void set_Ubox_undo_suu(int i) {
        Ubox.set_i_undo_total(i);
    }

    public void set_h_Ubox_undo_suu(int i) {
        h_Ubox.set_i_undo_total(i);
    }

    public void circle_organize() {//Organize all circles.
        foldLines.circle_organize();
    }

    public void add_hanten(Circle e0, Circle eh) {
        //e0の円周が(x,y)を通るとき
        if (Math.abs(OritaCalc.distance(e0.getCenter(), eh.getCenter()) - e0.getRadius()) < 0.0000001) {
            LineSegment s_add = new LineSegment();
            s_add.set(eh.turnAround_CircleToLineSegment(e0));
            //s_add.setcolor(3);
            addLineSegment(s_add);
            record();
            return;
        }

        //e0の円周が(x,y)を通らないとき。e0の円周の外部に(x,y)がくるとき//e0の円周の内部に(x,y)がくるとき
        Circle e_add = new Circle();
        e_add.set(eh.turnAround(e0));
        addCircle(e_add);
        record();
    }

    public void add_hanten(LineSegment s0, Circle eh) {
        StraightLine ty = new StraightLine(s0);
        //s0上に(x,y)がくるとき
        if (ty.calculateDistance(eh.getCenter()) < 0.0000001) {
            return;
        }

        //s0が(x,y)を通らないとき。
        Circle e_add = new Circle();
        e_add.set(eh.turnAround_LineSegmentToCircle(s0));
        addCircle(e_add);
        record();
    }

    public double get_d_decision_width() {
        return d_decision_width;
    }

    public void set_a_to_parallel_scale_interval(int i) {
        grid.set_a_to_parallel_scale_interval(i);
    }

    public void set_b_to_parallel_scale_interval(int i) {
        grid.set_b_to_parallel_scale_interval(i);
    }

    public void a_to_parallel_scale_position_change() {
        grid.a_to_parallel_scale_position_change();
    }

    public void b_to_parallel_scale_position_change() {
        grid.b_to_parallel_scale_position_change();
    }

    public enum FoldLineAdditionalInputMode {
        POLY_LINE_0,
        AUX_LINE_1,
        BLACK_LINE_2,
        AUX_LIVE_LINE_3,
        BOTH_4
    }

    public enum OperationFrameMode {
        NONE_0,
        CREATE_1,
        MOVE_POINTS_2,
        MOVE_SIDES_3,
        MOVE_BOX_4,
    }
}