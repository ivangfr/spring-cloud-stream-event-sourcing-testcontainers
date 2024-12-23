/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.ivanfranchin.userservice.messages;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class UserEventMessage extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -943644479468478271L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"UserEventMessage\",\"namespace\":\"com.ivanfranchin.userservice.messages\",\"fields\":[{\"name\":\"eventId\",\"type\":\"string\"},{\"name\":\"eventTimestamp\",\"type\":\"long\"},{\"name\":\"eventType\",\"type\":{\"type\":\"enum\",\"name\":\"EventType\",\"symbols\":[\"CREATED\",\"UPDATED\",\"DELETED\"]}},{\"name\":\"userId\",\"type\":\"long\"},{\"name\":\"userJson\",\"type\":[\"null\",\"string\"],\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<UserEventMessage> ENCODER =
      new BinaryMessageEncoder<>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<UserEventMessage> DECODER =
      new BinaryMessageDecoder<>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<UserEventMessage> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<UserEventMessage> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<UserEventMessage> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this UserEventMessage to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a UserEventMessage from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a UserEventMessage instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static UserEventMessage fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.lang.CharSequence eventId;
  private long eventTimestamp;
  private com.ivanfranchin.userservice.messages.EventType eventType;
  private long userId;
  private java.lang.CharSequence userJson;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public UserEventMessage() {}

  /**
   * All-args constructor.
   * @param eventId The new value for eventId
   * @param eventTimestamp The new value for eventTimestamp
   * @param eventType The new value for eventType
   * @param userId The new value for userId
   * @param userJson The new value for userJson
   */
  public UserEventMessage(java.lang.CharSequence eventId, java.lang.Long eventTimestamp, com.ivanfranchin.userservice.messages.EventType eventType, java.lang.Long userId, java.lang.CharSequence userJson) {
    this.eventId = eventId;
    this.eventTimestamp = eventTimestamp;
    this.eventType = eventType;
    this.userId = userId;
    this.userJson = userJson;
  }

  @Override
  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }

  @Override
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }

  // Used by DatumWriter.  Applications should not call.
  @Override
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return eventId;
    case 1: return eventTimestamp;
    case 2: return eventType;
    case 3: return userId;
    case 4: return userJson;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @Override
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: eventId = (java.lang.CharSequence)value$; break;
    case 1: eventTimestamp = (java.lang.Long)value$; break;
    case 2: eventType = (com.ivanfranchin.userservice.messages.EventType)value$; break;
    case 3: userId = (java.lang.Long)value$; break;
    case 4: userJson = (java.lang.CharSequence)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'eventId' field.
   * @return The value of the 'eventId' field.
   */
  public java.lang.CharSequence getEventId() {
    return eventId;
  }


  /**
   * Sets the value of the 'eventId' field.
   * @param value the value to set.
   */
  public void setEventId(java.lang.CharSequence value) {
    this.eventId = value;
  }

  /**
   * Gets the value of the 'eventTimestamp' field.
   * @return The value of the 'eventTimestamp' field.
   */
  public long getEventTimestamp() {
    return eventTimestamp;
  }


  /**
   * Sets the value of the 'eventTimestamp' field.
   * @param value the value to set.
   */
  public void setEventTimestamp(long value) {
    this.eventTimestamp = value;
  }

  /**
   * Gets the value of the 'eventType' field.
   * @return The value of the 'eventType' field.
   */
  public com.ivanfranchin.userservice.messages.EventType getEventType() {
    return eventType;
  }


  /**
   * Sets the value of the 'eventType' field.
   * @param value the value to set.
   */
  public void setEventType(com.ivanfranchin.userservice.messages.EventType value) {
    this.eventType = value;
  }

  /**
   * Gets the value of the 'userId' field.
   * @return The value of the 'userId' field.
   */
  public long getUserId() {
    return userId;
  }


  /**
   * Sets the value of the 'userId' field.
   * @param value the value to set.
   */
  public void setUserId(long value) {
    this.userId = value;
  }

  /**
   * Gets the value of the 'userJson' field.
   * @return The value of the 'userJson' field.
   */
  public java.lang.CharSequence getUserJson() {
    return userJson;
  }


  /**
   * Sets the value of the 'userJson' field.
   * @param value the value to set.
   */
  public void setUserJson(java.lang.CharSequence value) {
    this.userJson = value;
  }

  /**
   * Creates a new UserEventMessage RecordBuilder.
   * @return A new UserEventMessage RecordBuilder
   */
  public static com.ivanfranchin.userservice.messages.UserEventMessage.Builder newBuilder() {
    return new com.ivanfranchin.userservice.messages.UserEventMessage.Builder();
  }

  /**
   * Creates a new UserEventMessage RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new UserEventMessage RecordBuilder
   */
  public static com.ivanfranchin.userservice.messages.UserEventMessage.Builder newBuilder(com.ivanfranchin.userservice.messages.UserEventMessage.Builder other) {
    if (other == null) {
      return new com.ivanfranchin.userservice.messages.UserEventMessage.Builder();
    } else {
      return new com.ivanfranchin.userservice.messages.UserEventMessage.Builder(other);
    }
  }

  /**
   * Creates a new UserEventMessage RecordBuilder by copying an existing UserEventMessage instance.
   * @param other The existing instance to copy.
   * @return A new UserEventMessage RecordBuilder
   */
  public static com.ivanfranchin.userservice.messages.UserEventMessage.Builder newBuilder(com.ivanfranchin.userservice.messages.UserEventMessage other) {
    if (other == null) {
      return new com.ivanfranchin.userservice.messages.UserEventMessage.Builder();
    } else {
      return new com.ivanfranchin.userservice.messages.UserEventMessage.Builder(other);
    }
  }

  /**
   * RecordBuilder for UserEventMessage instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<UserEventMessage>
    implements org.apache.avro.data.RecordBuilder<UserEventMessage> {

    private java.lang.CharSequence eventId;
    private long eventTimestamp;
    private com.ivanfranchin.userservice.messages.EventType eventType;
    private long userId;
    private java.lang.CharSequence userJson;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.ivanfranchin.userservice.messages.UserEventMessage.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.eventId)) {
        this.eventId = data().deepCopy(fields()[0].schema(), other.eventId);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.eventTimestamp)) {
        this.eventTimestamp = data().deepCopy(fields()[1].schema(), other.eventTimestamp);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.eventType)) {
        this.eventType = data().deepCopy(fields()[2].schema(), other.eventType);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.userId)) {
        this.userId = data().deepCopy(fields()[3].schema(), other.userId);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.userJson)) {
        this.userJson = data().deepCopy(fields()[4].schema(), other.userJson);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
    }

    /**
     * Creates a Builder by copying an existing UserEventMessage instance
     * @param other The existing instance to copy.
     */
    private Builder(com.ivanfranchin.userservice.messages.UserEventMessage other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.eventId)) {
        this.eventId = data().deepCopy(fields()[0].schema(), other.eventId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.eventTimestamp)) {
        this.eventTimestamp = data().deepCopy(fields()[1].schema(), other.eventTimestamp);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.eventType)) {
        this.eventType = data().deepCopy(fields()[2].schema(), other.eventType);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.userId)) {
        this.userId = data().deepCopy(fields()[3].schema(), other.userId);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.userJson)) {
        this.userJson = data().deepCopy(fields()[4].schema(), other.userJson);
        fieldSetFlags()[4] = true;
      }
    }

    /**
      * Gets the value of the 'eventId' field.
      * @return The value.
      */
    public java.lang.CharSequence getEventId() {
      return eventId;
    }


    /**
      * Sets the value of the 'eventId' field.
      * @param value The value of 'eventId'.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder setEventId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.eventId = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'eventId' field has been set.
      * @return True if the 'eventId' field has been set, false otherwise.
      */
    public boolean hasEventId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'eventId' field.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder clearEventId() {
      eventId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'eventTimestamp' field.
      * @return The value.
      */
    public long getEventTimestamp() {
      return eventTimestamp;
    }


    /**
      * Sets the value of the 'eventTimestamp' field.
      * @param value The value of 'eventTimestamp'.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder setEventTimestamp(long value) {
      validate(fields()[1], value);
      this.eventTimestamp = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'eventTimestamp' field has been set.
      * @return True if the 'eventTimestamp' field has been set, false otherwise.
      */
    public boolean hasEventTimestamp() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'eventTimestamp' field.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder clearEventTimestamp() {
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'eventType' field.
      * @return The value.
      */
    public com.ivanfranchin.userservice.messages.EventType getEventType() {
      return eventType;
    }


    /**
      * Sets the value of the 'eventType' field.
      * @param value The value of 'eventType'.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder setEventType(com.ivanfranchin.userservice.messages.EventType value) {
      validate(fields()[2], value);
      this.eventType = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'eventType' field has been set.
      * @return True if the 'eventType' field has been set, false otherwise.
      */
    public boolean hasEventType() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'eventType' field.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder clearEventType() {
      eventType = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'userId' field.
      * @return The value.
      */
    public long getUserId() {
      return userId;
    }


    /**
      * Sets the value of the 'userId' field.
      * @param value The value of 'userId'.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder setUserId(long value) {
      validate(fields()[3], value);
      this.userId = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'userId' field has been set.
      * @return True if the 'userId' field has been set, false otherwise.
      */
    public boolean hasUserId() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'userId' field.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder clearUserId() {
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'userJson' field.
      * @return The value.
      */
    public java.lang.CharSequence getUserJson() {
      return userJson;
    }


    /**
      * Sets the value of the 'userJson' field.
      * @param value The value of 'userJson'.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder setUserJson(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.userJson = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'userJson' field has been set.
      * @return True if the 'userJson' field has been set, false otherwise.
      */
    public boolean hasUserJson() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'userJson' field.
      * @return This builder.
      */
    public com.ivanfranchin.userservice.messages.UserEventMessage.Builder clearUserJson() {
      userJson = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public UserEventMessage build() {
      try {
        UserEventMessage record = new UserEventMessage();
        record.eventId = fieldSetFlags()[0] ? this.eventId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.eventTimestamp = fieldSetFlags()[1] ? this.eventTimestamp : (java.lang.Long) defaultValue(fields()[1]);
        record.eventType = fieldSetFlags()[2] ? this.eventType : (com.ivanfranchin.userservice.messages.EventType) defaultValue(fields()[2]);
        record.userId = fieldSetFlags()[3] ? this.userId : (java.lang.Long) defaultValue(fields()[3]);
        record.userJson = fieldSetFlags()[4] ? this.userJson : (java.lang.CharSequence) defaultValue(fields()[4]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<UserEventMessage>
    WRITER$ = (org.apache.avro.io.DatumWriter<UserEventMessage>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<UserEventMessage>
    READER$ = (org.apache.avro.io.DatumReader<UserEventMessage>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.eventId);

    out.writeLong(this.eventTimestamp);

    out.writeEnum(this.eventType.ordinal());

    out.writeLong(this.userId);

    if (this.userJson == null) {
      out.writeIndex(0);
      out.writeNull();
    } else {
      out.writeIndex(1);
      out.writeString(this.userJson);
    }

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.eventId = in.readString(this.eventId instanceof Utf8 ? (Utf8)this.eventId : null);

      this.eventTimestamp = in.readLong();

      this.eventType = com.ivanfranchin.userservice.messages.EventType.values()[in.readEnum()];

      this.userId = in.readLong();

      if (in.readIndex() != 1) {
        in.readNull();
        this.userJson = null;
      } else {
        this.userJson = in.readString(this.userJson instanceof Utf8 ? (Utf8)this.userJson : null);
      }

    } else {
      for (int i = 0; i < 5; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.eventId = in.readString(this.eventId instanceof Utf8 ? (Utf8)this.eventId : null);
          break;

        case 1:
          this.eventTimestamp = in.readLong();
          break;

        case 2:
          this.eventType = com.ivanfranchin.userservice.messages.EventType.values()[in.readEnum()];
          break;

        case 3:
          this.userId = in.readLong();
          break;

        case 4:
          if (in.readIndex() != 1) {
            in.readNull();
            this.userJson = null;
          } else {
            this.userJson = in.readString(this.userJson instanceof Utf8 ? (Utf8)this.userJson : null);
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










