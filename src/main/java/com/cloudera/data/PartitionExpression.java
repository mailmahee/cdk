package com.cloudera.data;

import org.apache.commons.jexl2.Expression;
import org.apache.commons.jexl2.JexlContext;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class PartitionExpression {

  private JexlEngine engine;
  private Expression expression;
  private boolean isStrict;

  public PartitionExpression(String expression, boolean isStrict) {
    this.engine = new JexlEngine();
    this.engine.setStrict(true);
    this.engine.setSilent(false);
    this.engine.setCache(10);
    this.expression = engine.createExpression(expression);
    this.isStrict = isStrict;
  }

  public String evaluate(Object record) {
    JexlContext context = new MapContext();

    context.set("record", record);

    Object object = expression.evaluate(context);

    StringBuilder builder = new StringBuilder();

    if (object instanceof Object[]) {
      for (Object element : (Object[]) object) {
        if (builder.length() > 0) {
          builder.append("/");
        }

        builder.append(stringifyOrFail(element));
      }
    } else {
      builder.append(stringifyOrFail(object));
    }

    return builder.toString();
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("expression", expression)
        .add("isStrict", isStrict).add("engine", engine).toString();
  }

  private String stringifyOrFail(Object object) {
    if (isStrict) {
      Preconditions.checkArgument(object instanceof String,
          "Partition expression did not produce string result for value:%s",
          object);
    }

    return object.toString();
  }

}