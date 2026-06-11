package com.microservice.ingestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudEventDto {
 
    // CloudEvents standard fields
    private String specversion;   // always "1.0"
    private String type;          // e.g. "com.microservice.user.create" → resolves to Kafka topic
    private String source;        // where this event came from, e.g. "/connect-flow/user"
    private String id;            // unique event ID
 
    @JsonProperty("datacontenttype")
    private String dataContentType; // "application/json"
 
    // The actual payload — kept as raw JsonNode so ingestor
    // can forward it without knowing the exact schema at this point
    private JsonNode data;
}
