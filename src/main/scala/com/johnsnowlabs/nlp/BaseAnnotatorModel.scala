package com.johnsnowlabs.nlp

import org.apache.spark.ml.Model
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.ml.util.DefaultParamsWritable
import org.apache.spark.sql.types._

/**
  * This trait implements logic that applies nlp using Spark ML Pipeline transformers
  * Should strongly change once UsedDefinedTypes are allowed
  * https://issues.apache.org/jira/browse/SPARK-7768
  */
abstract class BaseAnnotatorModel[M <: Model[M]]
  extends Model[M]
    with DefaultParamsWritable
    with HasAnnotatorType
    with HasInputAnnotationCols
    with HasOutputAnnotationCol {

  /**
    * takes a document and annotations and produces new annotations of this annotator's annotation type
    * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
    * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
    */
  protected def annotate(annotations: Seq[Annotation]): Seq[Annotation]

  /** Shape of annotations at output */
  private def outputDataType: DataType = ArrayType(Annotation.dataType)

  /** requirement for pipeline transformation validation. It is called on fit() */
  override final def transformSchema(schema: StructType): StructType = {
    val metadataBuilder: MetadataBuilder = new MetadataBuilder()
    metadataBuilder.putString("annotatorType", annotatorType)
    val outputFields = schema.fields :+
      StructField(getOutputCol, outputDataType, nullable = false, metadataBuilder.build)
    StructType(outputFields)
  }

  /** requirement for annotators copies */
  override def copy(extra: ParamMap): M = defaultCopy(extra)

}