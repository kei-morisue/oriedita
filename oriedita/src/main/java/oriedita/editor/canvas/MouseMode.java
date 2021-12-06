package oriedita.editor.canvas;

public enum MouseMode {
    UNUSED_0(0),
    DRAW_CREASE_FREE_1(1),
    MOVE_CREASE_PATTERN_2(2),
    /**
     * Line segment deletion mode
     */
    LINE_SEGMENT_DELETE_3(3),
    CHANGE_CREASE_TYPE_4(4),
    LENGTHEN_CREASE_5(5),
    UNUSED_6(6),
    SQUARE_BISECTOR_7(7),
    INWARD_8(8),
    PERPENDICULAR_DRAW_9(9),
    SYMMETRIC_DRAW_10(10),
    DRAW_CREASE_RESTRICTED_11(11),
    DRAW_CREASE_SYMMETRIC_12(12),
    DRAW_CREASE_ANGLE_RESTRICTED_13(13),
    DRAW_POINT_14(14),
    DELETE_POINT_15(15),
    ANGLE_SYSTEM_16(16),
    DRAW_CREASE_ANGLE_RESTRICTED_2_17(17),
    DRAW_CREASE_ANGLE_RESTRICTED_3_18(18),
    CREASE_SELECT_19(19),
    CREASE_UNSELECT_20(20),
    CREASE_MOVE_21(21),
    CREASE_COPY_22(22),
    CREASE_MAKE_MOUNTAIN_23(23),
    CREASE_MAKE_VALLEY_24(24),
    CREASE_MAKE_EDGE_25(25),
    BACKGROUND_CHANGE_POSITION_26(26),
    LINE_SEGMENT_DIVISION_27(27),
    LINE_SEGMENT_RATIO_SET_28(28),
    POLYGON_SET_NO_CORNERS_29(29),
    CREASE_ADVANCE_TYPE_30(30),
    CREASE_MOVE_4P_31(31),
    CREASE_COPY_4P_32(32),
    FISH_BONE_DRAW_33(33),
    CREASE_MAKE_MV_34(34),
    DOUBLE_SYMMETRIC_DRAW_35(35),
    CREASES_ALTERNATE_MV_36(36),
    DRAW_CREASE_ANGLE_RESTRICTED_5_37(37),
    VERTEX_MAKE_ANGULARLY_FLAT_FOLDABLE_38(38),
    FOLDABLE_LINE_INPUT_39(39),
    PARALLEL_DRAW_40(40),
    VERTEX_DELETE_ON_CREASE_41(41),
    CIRCLE_DRAW_42(42),
    CIRCLE_DRAW_THREE_POINT_43(43),
    CIRCLE_DRAW_SEPARATE_44(44),
    CIRCLE_DRAW_TANGENT_LINE_45(45),
    CIRCLE_DRAW_INVERTED_46(46),
    CIRCLE_DRAW_FREE_47(47),
    CIRCLE_DRAW_CONCENTRIC_48(48),
    CIRCLE_DRAW_CONCENTRIC_SELECT_49(49),
    CIRCLE_DRAW_CONCENTRIC_TWO_CIRCLE_SELECT_50(50),
    PARALLEL_DRAW_WIDTH_51(51),
    CONTINUOUS_SYMMETRIC_DRAW_52(52),
    DISPLAY_LENGTH_BETWEEN_POINTS_1_53(53),
    DISPLAY_LENGTH_BETWEEN_POINTS_2_54(54),
    DISPLAY_ANGLE_BETWEEN_THREE_POINTS_1_55(55),
    DISPLAY_ANGLE_BETWEEN_THREE_POINTS_2_56(56),
    DISPLAY_ANGLE_BETWEEN_THREE_POINTS_3_57(57),
    CREASE_TOGGLE_MV_58(58),
    CIRCLE_CHANGE_COLOR_59(59),
    CREASE_MAKE_AUX_60(60),
    OPERATION_FRAME_CREATE_61(61),
    VORONOI_CREATE_62(62),
    FLAT_FOLDABLE_CHECK_63(63),
    CREASE_DELETE_OVERLAPPING_64(64),
    CREASE_DELETE_INTERSECTING_65(65),
    SELECT_POLYGON_66(66),
    UNSELECT_POLYGON_67(67),
    SELECT_LINE_INTERSECTING_68(68),
    UNSELECT_LINE_INTERSECTING_69(69),
    LENGTHEN_CREASE_SAME_COLOR_70(70),
    FOLDABLE_LINE_DRAW_71(71),

    MODIFY_CALCULATED_SHAPE_101(101),
    MOVE_CALCULATED_SHAPE_102(102),
    CHANGE_STANDARD_FACE_103(103),
    ADD_FOLDING_CONSTRAINT(104),

    UNUSED_10001(10001),
    UNUSED_10002(10002),
    ;

    int mode;

    MouseMode(int mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return Integer.toString(mode);
    }

    public String toReadableString() {
        return super.toString();
    }
}
