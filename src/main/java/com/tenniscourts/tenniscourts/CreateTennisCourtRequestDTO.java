package com.tenniscourts.tenniscourts;

import com.tenniscourts.schedules.ScheduleDTO;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class CreateTennisCourtRequestDTO {

    @NotNull
    private String name;

    private List<ScheduleDTO> tennisCourtSchedules;

}
