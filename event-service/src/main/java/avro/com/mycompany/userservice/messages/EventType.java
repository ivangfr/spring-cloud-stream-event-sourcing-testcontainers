/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.mycompany.userservice.messages;
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public enum EventType {
  CREATED, UPDATED, DELETED  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"EventType\",\"namespace\":\"com.mycompany.userservice.messages\",\"symbols\":[\"CREATED\",\"UPDATED\",\"DELETED\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
}