public enum MovementState {
    QUEUED, IN_CAR, PATRON_AT_EXHIBIT, GOING_CAR, GOING_BUNKER, IN_BUNKER, LEFT_PARK, // Patron movements
    IDLE, TO_EXHIBIT, CAR_AT_EXHIBIT, RETURNING, TO_BUNKER, CAR_AT_BUNKER, TO_IDLE // Car movements
}
