package com.patho.main.model.util.json;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TypeDef;

// http://blog.rohrpostix.net/postgresql-json-jsonb-type-to-be-used-with-hibernate/
@TypeDef(name = "JsonTest", typeClass = JsonTest.class)
@Getter
@Setter
public class JsonTest extends JsonType<JsonTest> {
	private String test;
}


//@Type(type = "MyType1JsonType")
//@Column(name="type_one")
//private MyType1 typeOne;