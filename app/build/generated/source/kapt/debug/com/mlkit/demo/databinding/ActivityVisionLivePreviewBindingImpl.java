package com.mlkit.demo.databinding;
import com.mlkit.demo.R;
import com.mlkit.demo.BR;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
@SuppressWarnings("unchecked")
public class ActivityVisionLivePreviewBindingImpl extends ActivityVisionLivePreviewBinding  {

    @Nullable
    private static final androidx.databinding.ViewDataBinding.IncludedLayouts sIncludes;
    @Nullable
    private static final android.util.SparseIntArray sViewsWithIds;
    static {
        sIncludes = null;
        sViewsWithIds = new android.util.SparseIntArray();
        sViewsWithIds.put(R.id.settings_button, 1);
        sViewsWithIds.put(R.id.iv_container, 2);
        sViewsWithIds.put(R.id.first_view, 3);
        sViewsWithIds.put(R.id.tv_information, 4);
        sViewsWithIds.put(R.id.preview_view, 5);
        sViewsWithIds.put(R.id.graphic_overlay, 6);
        sViewsWithIds.put(R.id.control, 7);
        sViewsWithIds.put(R.id.facing_switch, 8);
        sViewsWithIds.put(R.id.spinner, 9);
    }
    // views
    // variables
    // values
    // listeners
    // Inverse Binding Event Handlers

    public ActivityVisionLivePreviewBindingImpl(@Nullable androidx.databinding.DataBindingComponent bindingComponent, @NonNull View root) {
        this(bindingComponent, root, mapBindings(bindingComponent, root, 10, sIncludes, sViewsWithIds));
    }
    private ActivityVisionLivePreviewBindingImpl(androidx.databinding.DataBindingComponent bindingComponent, View root, Object[] bindings) {
        super(bindingComponent, root, 0
            , (android.view.View) bindings[7]
            , (android.widget.ToggleButton) bindings[8]
            , (android.view.View) bindings[3]
            , (com.mlkit.demo.GraphicOverlay) bindings[6]
            , (android.widget.ImageView) bindings[2]
            , (com.mlkit.demo.CameraSourcePreview) bindings[5]
            , (androidx.constraintlayout.widget.ConstraintLayout) bindings[0]
            , (android.view.View) bindings[1]
            , (android.widget.Spinner) bindings[9]
            , (android.widget.TextView) bindings[4]
            );
        this.rootLayout.setTag(null);
        setRootTag(root);
        // listeners
        invalidateAll();
    }

    @Override
    public void invalidateAll() {
        synchronized(this) {
                mDirtyFlags = 0x1L;
        }
        requestRebind();
    }

    @Override
    public boolean hasPendingBindings() {
        synchronized(this) {
            if (mDirtyFlags != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setVariable(int variableId, @Nullable Object variable)  {
        boolean variableSet = true;
            return variableSet;
    }

    @Override
    protected boolean onFieldChange(int localFieldId, Object object, int fieldId) {
        switch (localFieldId) {
        }
        return false;
    }

    @Override
    protected void executeBindings() {
        long dirtyFlags = 0;
        synchronized(this) {
            dirtyFlags = mDirtyFlags;
            mDirtyFlags = 0;
        }
        // batch finished
    }
    // Listener Stub Implementations
    // callback impls
    // dirty flag
    private  long mDirtyFlags = 0xffffffffffffffffL;
    /* flag mapping
        flag 0 (0x1L): null
    flag mapping end*/
    //end
}