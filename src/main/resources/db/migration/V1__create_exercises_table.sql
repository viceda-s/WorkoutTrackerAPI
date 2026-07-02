CREATE TABLE exercises (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('STRENGTH', 'CARDIO', 'FLEXIBILITY')),
    muscle_group VARCHAR(20) CHECK (muscle_group IN ('CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE'))
);

INSERT INTO exercises (name, description, type, muscle_group) VALUES
    ('Bench Press', 'A compound chest exercise performed lying on a bench, pressing a barbell upward.', 'STRENGTH', 'CHEST'),
    ('Push-Up', 'A bodyweight exercise that targets the chest, shoulders, and triceps by lowering and raising the body.', 'STRENGTH', 'CHEST'),
    ('Squat', 'A lower body compound movement where you lower your hips from a standing position and then stand back up.', 'STRENGTH', 'LEGS'),
    ('Leg Press', 'A machine-based strength exercise where the user pushes a weight away using their legs.', 'STRENGTH', 'LEGS'),
    ('Lunges', 'A single-leg strength exercise involving stepping forward, backward, or laterally to bend the knees.', 'STRENGTH', 'LEGS'),
    ('Deadlift', 'A compound weight training exercise where a loaded barbell is lifted off the ground to the level of the hips.', 'STRENGTH', 'BACK'),
    ('Pull-Up', 'An upper-body bodyweight exercise where you hang from a bar and pull your chin over the bar.', 'STRENGTH', 'BACK'),
    ('Lat Pulldown', 'A machine-based pulling exercise designed to develop the latissimus dorsi muscle in the back.', 'STRENGTH', 'BACK'),
    ('Overhead Press', 'A weight training exercise where a weight is pressed straight upwards from the front of the shoulders.', 'STRENGTH', 'SHOULDERS'),
    ('Lateral Raise', 'An isolation exercise involving lifting weights away from the body to the side to target the deltoids.', 'STRENGTH', 'SHOULDERS'),
    ('Bicep Curl', 'An isolation exercise that primarily targets the biceps brachii muscle by flexing the elbow.', 'STRENGTH', 'ARMS'),
    ('Tricep Dip', 'A bodyweight exercise focusing on the triceps by lowering and raising the body using parallel bars.', 'STRENGTH', 'ARMS'),
    ('Plank', 'An isometric core exercise that involves maintaining a position similar to a push-up for a set time.', 'STRENGTH', 'CORE'),
    ('Sit-Up', 'An abdominal endurance training exercise to strengthen and tone the core muscles.', 'STRENGTH', 'CORE'),
    ('Running', 'A high-impact cardiovascular exercise involving rapid, continuous movement on foot.', 'CARDIO', NULL),
    ('Cycling', 'A low-impact aerobic exercise performed on a bicycle or stationary bike.', 'CARDIO', NULL),
    ('Rowing', 'A full-body cardiovascular workout simulating the action of rowing a boat.', 'CARDIO', NULL),
    ('Jump Rope', 'A cardio conditioning exercise involving repeatedly jumping over a swinging rope.', 'CARDIO', NULL),
    ('Static Hamstring Stretch', 'A flexibility exercise to stretch the back of the thigh by holding a forward bend.', 'FLEXIBILITY', 'LEGS'),
    ('Cat-Cow Stretch', 'A gentle flow between two poses that warms the body and brings flexibility to the spine.', 'FLEXIBILITY', 'BACK');