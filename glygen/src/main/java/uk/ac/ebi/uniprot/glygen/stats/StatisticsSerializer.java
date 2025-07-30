package uk.ac.ebi.uniprot.glygen.stats;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.ArrayList;

public class StatisticsSerializer extends StdSerializer<Statistics> {

    public StatisticsSerializer() {
        this(null);
    }

    private StatisticsSerializer(Class<Statistics> t) {
        super(t);
    }

    @Override
    public void serialize(
            Statistics statistics, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {

        jgen.writeStartObject();
        jgen.writeObjectField("class", statistics.getClassMap());
        jgen.writeObjectField("predicate", statistics.getPredicateMap());
        jgen.writeObjectField("classpredicate", statistics.getClassPredicateMap());
        jgen.writeObjectField("classpredicatevalue", statistics.getClassPredicateValueMap());
        jgen.writeObjectField("predicateValue", new ArrayList<>());
        jgen.writeObjectField("nsmap", statistics.getNsMap());
        jgen.writeEndObject();
    }
}
