package origami_editor.editor.hierarchylist_worker.hierarchylist;


import origami_editor.editor.hierarchylist_worker.hierarchylist.equivalence_condition.EquivalenceCondition;

import java.util.ArrayList;

public class HierarchyList {//This class is used to record and utilize the hierarchical relationship of faces when folded.
    int facesTotal;             //Number of faces in the unfolded view before folding

    // hierarchyList[][] treats the hierarchical relationship between all the faces of the crease pattern before folding as one table.
    // If hierarchyList[i][j] is 1, surface i is above surface j. If it is 0, it is the lower side.
    // If hierarchyList[i][j] is -50, faces i and j overlap, but the hierarchical relationship is not determined.
    // If hierarchyList[i][j] is -100, then faces i and j do not overlap.
    HierarchyListCondition[][] hierarchyList;
    HierarchyListCondition[][] hierarchyList_copy;
    int EquivalenceConditionTotal;   //A combination that can cause a situation in which another surface penetrates the boundary between two adjacent surfaces.
    ArrayList<EquivalenceCondition> tL = new ArrayList<>();
    int uEquivalenceConditionTotal;
    ArrayList<EquivalenceCondition> uL = new ArrayList<>();

    public HierarchyList() {
        reset();
    }
    //Furthermore, when a, b, c, and d coexist in a certain SubFace, a combination that can cause penetration at the boundary line

    public void reset() {
        tL.clear();
        tL.add(new EquivalenceCondition());
        uL.clear();
        uL.add(new EquivalenceCondition());
        EquivalenceConditionTotal = 0;
        uEquivalenceConditionTotal = 0;
    }

    public void save() {
        for (int i = 1; i <= facesTotal; i++) {
            for (int j = 1; j <= facesTotal; j++) {
                hierarchyList_copy[i][j] = hierarchyList[i][j];
            }
        }
    }

    public void restore() {
        for (int i = 1; i <= facesTotal; i++) {
            for (int j = 1; j <= facesTotal; j++) {
                hierarchyList[i][j] = hierarchyList_copy[i][j];
            }
        }
    }

    public void set(int i, int j, HierarchyListCondition condition) {
        hierarchyList[i][j] = condition;
    }

    public HierarchyListCondition get(int i, int j) {
        return hierarchyList[i][j];
    }

    public int getFacesTotal() {
        return facesTotal;
    }

    public void setFacesTotal(int iM) {
        facesTotal = iM;

        hierarchyList = new HierarchyListCondition[facesTotal + 1][facesTotal + 1];
        hierarchyList_copy = new HierarchyListCondition[facesTotal + 1][facesTotal + 1];

        for (int i = 0; i <= facesTotal; i++) {
            for (int j = 0; j <= facesTotal; j++) {
                hierarchyList[i][j] = HierarchyListCondition.EMPTY_N100;
                hierarchyList_copy[i][j] = HierarchyListCondition.EMPTY_N100;
            }
        }
    }

    public int getEquivalenceConditionTotal() {
        return EquivalenceConditionTotal;
    }

    public EquivalenceCondition getEquivalenceCondition(int i) {
        return tL.get(i);
    }

    // Add equivalence condition. When there are two adjacent faces im1 and im2 as the boundary of the bar ib, when the folding is estimated
    // The surface im located at the position where it overlaps a part of the bar ib is not sandwiched between the surface im1 and the surface im2 in the vertical direction. From this
    // The equivalent condition of gj [im1] [im] = gj [im2] [im] is satisfied.
    public void addEquivalenceCondition(int ai, int bi, int ci, int di) {
        EquivalenceConditionTotal = EquivalenceConditionTotal + 1;
        tL.add(new EquivalenceCondition(ai, bi, ci, di));
    }

    public int getUEquivalenceConditionTotal() {
        return uEquivalenceConditionTotal;
    }

    public EquivalenceCondition getUEquivalenceCondition(int i) {
        return uL.get(i);
    }

    // Add equivalence condition. There are two adjacent faces im1 and im2 as the boundary of the bar ib,
    // Also, there are two adjacent faces im3 and im4 as the boundary of the bar jb, and when ib and jb are parallel and partially overlap, when folding is estimated.
    // The surface of the bar ib and the surface of the surface jb are not aligned with i, j, i, j or j, i, j, i. If this happens,
    // Since there is a mistake in the 3rd place from the beginning, find out what digit this 3rd place is in SubFace and advance this digit by 1.
    public void addUEquivalenceCondition(int ai, int bi, int ci, int di) {
        uEquivalenceConditionTotal = uEquivalenceConditionTotal + 1;

        uL.add(new EquivalenceCondition(ai, bi, ci, di));
    }

    public enum HierarchyListCondition {
        UNKNOWN_0,
        UNKNOWN_1,
        UNKNOWN_N50,
        EMPTY_N100,
        ;

        public boolean isEmpty() {
            return this == UNKNOWN_N50 || this == EMPTY_N100;
        }
    }
}
