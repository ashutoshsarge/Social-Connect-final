package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.EventDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.EventEntity
import com.example.data.local.entity.SubtaskEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        EventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sevaconnect_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val userDao = database.userDao()
            val taskDao = database.taskDao()
            val eventDao = database.eventDao()

            if (userDao.getUserCount() > 0) return

            // Seed Users
            val diyaId = userDao.insertUser(
                UserEntity(
                    id = 1,
                    email = "diyasarge@gmail.com",
                    passwordHash = "seva123",
                    fullName = "Diya Sarge",
                    role = "VOLUNTEER",
                    organization = "Delhi Youth Volunteers",
                    volunteerHours = 42,
                    badges = "Eco Champion,Weekend Hero,7-Day Streak,Verified Volunteer",
                    phone = "+91 98765 43210"
                )
            )

            val priyaId = userDao.insertUser(
                UserEntity(
                    id = 4,
                    email = "priya@sevaconnect.org",
                    passwordHash = "seva123",
                    fullName = "Priya Sharma",
                    role = "VOLUNTEER",
                    organization = "Delhi Youth Volunteers",
                    volunteerHours = 35,
                    badges = "Eco Champion,Weekend Hero",
                    phone = "+91 98112 34567"
                )
            )

            val aaravId = userDao.insertUser(
                UserEntity(
                    id = 2,
                    email = "aarav@goonj.org",
                    passwordHash = "admin123",
                    fullName = "Aarav Patel",
                    role = "NGO_LEADER",
                    organization = "Goonj Seva Foundation",
                    volunteerHours = 120,
                    badges = "Community Leader,Master Organizer,500+ Hours",
                    phone = "+91 98223 45678"
                )
            )

            val rahulId = userDao.insertUser(
                UserEntity(
                    id = 3,
                    email = "rahul@sevaconnect.org",
                    passwordHash = "seva123",
                    fullName = "Rahul Verma",
                    role = "VOLUNTEER",
                    organization = "Green Earth Club",
                    volunteerHours = 28,
                    badges = "Literacy Scout,Eco Warrior",
                    phone = "+91 98334 56789"
                )
            )

            val ananyaId = userDao.insertUser(
                UserEntity(
                    id = 5,
                    email = "ananya@sevaconnect.org",
                    passwordHash = "seva123",
                    fullName = "Ananya Sen",
                    role = "VOLUNTEER",
                    organization = "CareIndia Volunteer Circle",
                    volunteerHours = 35,
                    badges = "Health Ally,Green Guardian",
                    phone = "+91 98445 67890"
                )
            )

            // Seed Events
            val event1 = EventEntity(
                id = 1,
                title = "Yamuna River Clean-up & Tree Plantation",
                ngoName = "Goonj Seva Foundation",
                category = "ENVIRONMENT",
                date = "This Saturday, 7:00 AM",
                location = "Yamuna Ghat Sector 15",
                requiredVolunteers = 50,
                registeredVolunteers = 38,
                description = "Join hands to restore the river banks, collect non-biodegradable waste, and plant 200 indigenous saplings.",
                isJoined = true
            )

            val event2 = EventEntity(
                id = 2,
                title = "Teach for Tomorrow - Weekend Literacy Camp",
                ngoName = "Robin Hood Army",
                category = "EDUCATION",
                date = "Sunday, 10:00 AM",
                location = "Govt Primary School, Okhla",
                requiredVolunteers = 25,
                registeredVolunteers = 21,
                description = "Interactive storytelling, math games, and distributing school kits to underprivileged kids.",
                isJoined = false
            )

            val event3 = EventEntity(
                id = 3,
                title = "Warmth Distribution - Winter Blanket Drive",
                ngoName = "Seva Bharti",
                category = "COMMUNITY",
                date = "Next Friday, 8:00 PM",
                location = "Old Delhi Railway Shelter",
                requiredVolunteers = 30,
                registeredVolunteers = 16,
                description = "Distribute insulated thermal blankets and hot meal packets to homeless night shelters.",
                isJoined = true
            )

            val event4 = EventEntity(
                id = 4,
                title = "Free Eye & Dental Care Health Camp",
                ngoName = "CareIndia NGO",
                category = "HEALTH",
                date = "Next Saturday, 9:30 AM",
                location = "Community Hall, Rohini",
                requiredVolunteers = 20,
                registeredVolunteers = 18,
                description = "Assist doctors with patient queue management, recording vitals, and dispensing prescription glasses.",
                isJoined = false
            )

            eventDao.insertEvents(listOf(event1, event2, event3, event4))

            // Seed Tasks
            val task1Id = taskDao.insertTask(
                TaskEntity(
                    id = 1,
                    title = "Assemble 200 Educational Book & Stationery Kits",
                    description = "Package donated notebooks, geometry sets, and storybooks for the Okhla learning camp.",
                    status = "IN_PROGRESS",
                    priority = "HIGH",
                    category = "EDUCATION",
                    assignedToId = priyaId,
                    assignedToName = "Priya Sharma",
                    createdById = aaravId,
                    createdByName = "Aarav Patel",
                    dueDate = "Tomorrow, 4:00 PM",
                    progressPercent = 50,
                    estimatedHours = 4
                )
            )

            taskDao.insertSubtasks(
                listOf(
                    SubtaskEntity(taskId = task1Id, title = "Collect books from donor drop points", isCompleted = true),
                    SubtaskEntity(taskId = task1Id, title = "Sort by grade 1-5 curriculum", isCompleted = true),
                    SubtaskEntity(taskId = task1Id, title = "Pack into branded Seva bags", isCompleted = false),
                    SubtaskEntity(taskId = task1Id, title = "Seal and count final inventory", isCompleted = false)
                )
            )

            val task2Id = taskDao.insertTask(
                TaskEntity(
                    id = 2,
                    title = "Setup Waste Segregation & Safety Station at Ghat",
                    description = "Deploy colour-coded waste bins, safety gloves, medical first-aid kit, and hydration points.",
                    status = "TODO",
                    priority = "URGENT",
                    category = "ENVIRONMENT",
                    assignedToId = rahulId,
                    assignedToName = "Rahul Verma",
                    createdById = aaravId,
                    createdByName = "Aarav Patel",
                    dueDate = "Today, 6:00 PM",
                    progressPercent = 0,
                    estimatedHours = 3
                )
            )

            taskDao.insertSubtasks(
                listOf(
                    SubtaskEntity(taskId = task2Id, title = "Procure 30 reusable heavy-duty sacks", isCompleted = false),
                    SubtaskEntity(taskId = task2Id, title = "Inspect rubber gloves & tongs inventory", isCompleted = false),
                    SubtaskEntity(taskId = task2Id, title = "Coordinate garbage van arrival with municipality", isCompleted = false)
                )
            )

            val task3Id = taskDao.insertTask(
                TaskEntity(
                    id = 3,
                    title = "Verify Volunteer Onboarding & Health Waivers",
                    description = "Validate digital consent forms and emergency contact info for 40 registered volunteers.",
                    status = "IN_PROGRESS",
                    priority = "MEDIUM",
                    category = "HEALTH",
                    assignedToId = ananyaId,
                    assignedToName = "Ananya Sen",
                    createdById = aaravId,
                    createdByName = "Aarav Patel",
                    dueDate = "Friday, 2:00 PM",
                    progressPercent = 66,
                    estimatedHours = 2
                )
            )

            taskDao.insertSubtasks(
                listOf(
                    SubtaskEntity(taskId = task3Id, title = "Verify parent consents for youth volunteers", isCompleted = true),
                    SubtaskEntity(taskId = task3Id, title = "Check tetanus booster confirmations", isCompleted = true),
                    SubtaskEntity(taskId = task3Id, title = "Print high-vis volunteer badges", isCompleted = false)
                )
            )

            val task4Id = taskDao.insertTask(
                TaskEntity(
                    id = 4,
                    title = "Procure 300 Thermal Blankets & Nutrition Bars",
                    description = "Coordinate with wholesale suppliers in Chandni Chowk and transport to central storage.",
                    status = "REVIEW",
                    priority = "HIGH",
                    category = "COMMUNITY",
                    assignedToId = priyaId,
                    assignedToName = "Priya Sharma",
                    createdById = aaravId,
                    createdByName = "Aarav Patel",
                    dueDate = "Thursday, 7:00 PM",
                    progressPercent = 100,
                    estimatedHours = 5
                )
            )

            taskDao.insertSubtasks(
                listOf(
                    SubtaskEntity(taskId = task4Id, title = "Supplier price quote negotiation", isCompleted = true),
                    SubtaskEntity(taskId = task4Id, title = "Fleece thickness quality check", isCompleted = true),
                    SubtaskEntity(taskId = task4Id, title = "Transport via logistics tempo", isCompleted = true)
                )
            )

            val task5Id = taskDao.insertTask(
                TaskEntity(
                    id = 5,
                    title = "Publish Post-Drive Impact Metrics & Certificates",
                    description = "Compile photos, calculate total waste diverted from river, and issue e-certificates to volunteers.",
                    status = "DONE",
                    priority = "LOW",
                    category = "COMMUNITY",
                    assignedToId = priyaId,
                    assignedToName = "Priya Sharma",
                    createdById = aaravId,
                    createdByName = "Aarav Patel",
                    dueDate = "Completed Yesterday",
                    progressPercent = 100,
                    estimatedHours = 3
                )
            )

            taskDao.insertSubtasks(
                listOf(
                    SubtaskEntity(taskId = task5Id, title = "Weigh waste sacks at weighbridge", isCompleted = true),
                    SubtaskEntity(taskId = task5Id, title = "Generate automated PDF certificates", isCompleted = true),
                    SubtaskEntity(taskId = task5Id, title = "Email impact summary to donors", isCompleted = true)
                )
            )
        }
    }
}
