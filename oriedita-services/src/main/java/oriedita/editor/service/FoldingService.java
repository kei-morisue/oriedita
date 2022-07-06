package oriedita.editor.service;

import oriedita.editor.Foldable;
import origami.crease_pattern.FoldingException;
import origami.crease_pattern.LineSegmentSet;
import origami.folding.FoldedFigure;

public interface FoldingService {
    void folding_estimated(Foldable selectedFigure) throws InterruptedException, FoldingException;

    void fold(FoldedFigure.EstimationOrder estimationOrder);

    FoldType getFoldType();

    Foldable initFoldedFigure();

    void createTwoColoredCp();

    void foldAnother(Foldable selectedItem);

    LineSegmentSet getLineSegmentsForFolding();

    enum FoldType {
        FOR_ALL_CONNECTED_LINES_1,
        FOR_SELECTED_LINES_2
    }
}
