package com.game.game.domain;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uroczysko {

    private long id;

    private boolean flipped;

    public void flip() {
        flipped = !flipped;
    }
}