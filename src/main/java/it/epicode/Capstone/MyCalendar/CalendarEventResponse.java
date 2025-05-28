package it.epicode.Capstone.MyCalendar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventResponse {
    private Long id;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String source;
    private String googleEventId;
}