package oriedita.editor.export;

import fold.Exporter;
import fold.FoldFileFormatException;
import fold.Importer;
import fold.impl.CustomImporter;
import fold.impl.DefaultExporter;
import fold.model.*;
import oriedita.editor.exception.FileReadingException;
import oriedita.editor.save.OrieditaFoldFile;
import oriedita.editor.save.Save;
import oriedita.editor.save.SaveProvider;
import oriedita.util.VersionUtil;
import origami.crease_pattern.FoldLineSet;
import origami.crease_pattern.LineSegmentSet;
import origami.crease_pattern.PointSet;
import origami.crease_pattern.element.LineColor;
import origami.crease_pattern.element.LineSegment;
import origami.crease_pattern.element.Point;
import origami.crease_pattern.worker.WireFrame_Worker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
public class Fold {
    private final Importer<OrieditaFoldFile> anImporter;
    private final Exporter<OrieditaFoldFile> exporter;

    @Inject
    public Fold() {
        this.anImporter = new CustomImporter<>(OrieditaFoldFile.class);
        this.exporter = new DefaultExporter<>();
    }

    public Save toSave(OrieditaFoldFile foldFile) {
        Save save = SaveProvider.createInstance();

        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < foldFile.getEdges().size(); i++) {
            Edge edge = foldFile.getEdges().get(i);

            LineSegment ls = new LineSegment();
            double ax = edge.getStart().getX();
            double ay = edge.getStart().getY();
            ls.setA(new Point(ax, ay));
            double bx = edge.getEnd().getX();
            double by = edge.getEnd().getY();
            ls.setB(new Point(bx, by));
            ls.setColor(getColor(edge.getAssignment()));

            minX = Math.min(Math.min(minX, ax), bx);
            minY = Math.min(Math.min(minY, ay), by);
            maxX = Math.max(Math.max(maxX, ax), bx);
            maxY = Math.max(Math.max(maxY, ay), by);

            save.addLineSegment(ls);
        }

        save.setCircles(new ArrayList<>(foldFile.getCircles()));

        FoldLineSet ori_s_temp = new FoldLineSet();    //セレクトされた折線だけ取り出すために使う
        ori_s_temp.setSave(save);//セレクトされた折線だけ取り出してori_s_tempを作る
        ori_s_temp.move(
                new Point(minX, minY),
                new Point(minX, maxY),
                new Point(-200, -200),
                new Point(-200, 200)
        );

        Save save1 = SaveProvider.createInstance();
        ori_s_temp.getSave(save1);

        save1.setTexts(new ArrayList<>(foldFile.getTexts()));

        return save1;
    }

    private LineColor getColor(FoldEdgeAssignment edgeAssignment) {
        switch (edgeAssignment) {
            case BORDER:
                return LineColor.BLACK_0;
            case MOUNTAIN_FOLD:
                return LineColor.RED_1;
            case VALLEY_FOLD:
                return LineColor.BLUE_2;
            case FLAT_FOLD:
                return LineColor.CYAN_3;
            case UNASSIGNED:
            default:
                return LineColor.BLACK_0;
        }
    }

    private FoldEdgeAssignment getAssignment(LineColor lineColor) {
        switch (lineColor) {
            case ANGLE:
            case NONE:
            default:
                return FoldEdgeAssignment.UNASSIGNED;
            case BLACK_0:
                return FoldEdgeAssignment.BORDER;
            case RED_1:
                return FoldEdgeAssignment.MOUNTAIN_FOLD;
            case BLUE_2:
                return FoldEdgeAssignment.VALLEY_FOLD;
            case CYAN_3:
            case ORANGE_4:
            case MAGENTA_5:
            case GREEN_6:
            case YELLOW_7:
            case PURPLE_8:
            case OTHER_9:
                return FoldEdgeAssignment.FLAT_FOLD;
        }
    }

    public Save importFile(File file) throws FileReadingException {
        try {
            return toSave(anImporter.importFile(file));
        } catch (FoldFileFormatException e) {
            throw new FileReadingException(e);
        }
    }


    public void exportFile(Save save, LineSegmentSet lineSegmentSet, File file) throws InterruptedException, FileReadingException {
        try {
            exporter.exportFile(file, toFoldSave(save, lineSegmentSet));
        } catch (FoldFileFormatException e) {
            throw new FileReadingException(e);
        }
    }

    public OrieditaFoldFile toFoldSave(Save save, LineSegmentSet lineSegmentSet) throws InterruptedException {
        WireFrame_Worker wireFrame_worker = new WireFrame_Worker(3.0);
        wireFrame_worker.setLineSegmentSet(lineSegmentSet);

        PointSet pointSet = wireFrame_worker.get();

        OrieditaFoldFile foldFile = new OrieditaFoldFile();

        for (int i = 1; i <= pointSet.getNumPoints(); i++) {
            Vertex vertex = new Vertex();
            vertex.setId(i-1);
            vertex.setX(pointSet.getPoint(i).getX());
            vertex.setY(pointSet.getPoint(i).getY());
            foldFile.getVertices().add(vertex);
        }

        for (int i = 1; i <= pointSet.getNumLines(); i++) {
            Edge edge = new Edge();
            edge.setAssignment(getAssignment(pointSet.getColor(i)));
            edge.setFoldAngle(getFoldAngle(pointSet.getColor(i)));
            Vertex startVertex = foldFile.getVertices().get(pointSet.getBegin(i)-1);
            Vertex endVertex = foldFile.getVertices().get(pointSet.getEnd(i)-1);

            edge.setStart(startVertex);
            edge.setEnd(endVertex);

            edge.setId(i);

            foldFile.getEdges().add(edge);
        }

        foldFile.setCircles(save.getCircles());
        foldFile.setTexts(save.getTexts());
        foldFile.setVersion(VersionUtil.getVersionFromManifest());

        return foldFile;
    }

    private double getFoldAngle(LineColor color) {
        switch (color) {
            case BLUE_2:
                return 180;
            case RED_1:
                return -180;
            default:
                return 0;
        }
    }

    private List<Double> toFoldPoint(Point p) {
        return Arrays.asList(p.getX(), p.getY());
    }
}
