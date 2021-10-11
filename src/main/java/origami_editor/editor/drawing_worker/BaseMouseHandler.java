package origami_editor.editor.drawing_worker;

public abstract class BaseMouseHandler implements MouseModeHandler {
    protected DrawingWorker d;

    public BaseMouseHandler() { }

    public void setDrawingWorker(DrawingWorker d) {
        this.d = d;
    }
}
