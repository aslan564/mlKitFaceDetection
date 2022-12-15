package androidx.databinding;

public class DataBinderMapperImpl extends MergedDataBinderMapper {
  DataBinderMapperImpl() {
    addMapper(new com.google.mlkit.vision.demo.DataBinderMapperImpl());
  }
}
