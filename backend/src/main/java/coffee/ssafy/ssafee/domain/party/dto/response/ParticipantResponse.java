package coffee.ssafy.ssafee.domain.party.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ParticipantResponse(
        Long id,
        String name,
        Boolean isCarrier,
        Boolean paid
) {
}
