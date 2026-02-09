package com.sma.core.dto.response.resume;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeSkillGroupResponse {
    Integer id;
    String name;
    Integer orderIndex;
    List<ResumeSkillDetailResponse> skills;
}
