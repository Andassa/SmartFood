package com.mycompany.smartfood.domain.coverage;

final class StockMatch extends LineHandler {

    StockMatch(LineHandler next) {
        super(next);
    }

    @Override
    Match attempt(Request request) {
        String key = request.pantry().resolve(request.name(), request.need().dimension());
        return key != null && request.pantry().take(key, request.need()) ? Match.covered() : null;
    }
}
