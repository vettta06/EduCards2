package com.example.educards2.database

import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [Card::class, Stats::class, Deck::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun statsDao(): StatsDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cards_database"
                )
                    /*.addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            GlobalScope.launch(Dispatchers.IO) {
                                val cards = getDefaultCards()
                                getDatabase(context).cardDao().insertAll(cards)
                            }
                        }
                    })*/
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /*private fun getDefaultCards(): List<Card> {
            val linearAlgebraDeck = Deck(
                id = 1,
                name = "Линейная алгебра",
                isBuiltIn = true
            )

            val mathAnalysisDeck = Deck(
                id = 2,
                name = "Математический анализ",
                isBuiltIn = true
            )

            val oopDeck = Deck(
                id = 3,
                name = "ООП",
                isBuiltIn = true
            )

            GlobalScope.launch(Dispatchers.IO) {
                INSTANCE?.deckDao()?.apply {
                    insert(linearAlgebraDeck)
                    insert(mathAnalysisDeck)
                    insert(oopDeck)
                }
            }

            return listOf(
                Card(
                    deckId = 1,
                    question = "Что такое ранг матрицы?",
                    answer = "Максимальное число линейно независимых строк или столбцов",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Как проверить, что векторы ортогональны?",
                    answer = "Их скалярное произведение равно нулю",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Что означает det(A) = 0?",
                    answer = "Матрица A вырожденная (не имеет обратной)",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Что такое координаты вектора в заданном базиса?",
                    answer = "Координаты вектора — это коэффициенты, с которыми базисные векторы входят в его линейную комбинацию",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Как найти матрицу перехода от одного базиса к другому?",
                    answer = "Матрица перехода состоит из координат векторов старого базиса, выраженных через новый базис",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Может ли пространство многочленов степени ≤ 2 иметь базис из трех элементов?",
                    answer = "Да, например {1, x, x^2}",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Что такое ядро линейного оператора?",
                    answer = "Ядро оператора — множество векторов v, для которых Av = 0",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Как найти собственные значения матрицы?",
                    answer = "Решить уравнение: det(A−λE)=0",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Что означает диагонализируемость матрицы?",
                    answer = "Матрица диагонализируема, если существует базис из собственных векторов",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Что такое направляющий вектор прямой?",
                    answer = "Это ненулевой вектор, параллельный прямой",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Какой вектор является собственным для тождественного оператора?",
                    answer = "Любой ненулевой вектор является собственным для тождественного оператора",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = "Что такое базис линейного пространства L?",
                    answer = "Упорядоченная система векторов {e⃗1, … , e⃗n} ∈ L называется базисом линейного пространства L, если она линейно независимая и полная.",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 1,
                    question = " Как связаны координаты произведения вектора на число и координаты самого вектора?",
                    answer = "Координаты произведения вектора на число равны произведению его координат на число",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 1,
                    question = "Что можно сказать о числе векторов в различных базисах одного и того же линейного пространства?",
                    answer = "Различные базисы в линейном пространстве состоят из одинакового числа векторов",
                    isBuiltIn = true
                ),


                Card(
                    deckId = 2,
                    question = "Что такое производная?",
                    answer = "Скорость изменения функции в точке",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Как найти предел функции?",
                    answer = "Подставить точку, если неопределённость — упростить или использовать правило Лопиталя",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Чему равен интеграл ∫ 0 dx?",
                    answer = "∫ 0 dx = C",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Чему равен интеграл ∫ 1 dx?",
                    answer = "∫ 1 dx = x + C",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Если F(x) - первообразная для f(x), как выглядит общий вид для всех ее первообразных?",
                    answer = " Если F(x) – одна из первообразных для функции f(x), то выражение F(x) + C, где С – произвольная постоянная, представляет собой общий вид для всех ее первообразных.",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 2,
                    question = "Что вычисляет интеграл?",
                    answer = "Площадь под кривой или первообразную функции",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Что такое подынтегральная функция и подынтегральное выражение?",
                    answer = "Функция f(x) называется подынтегральной функцией, а выражение f(x)dx – подынтегральным выражением.",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = " Какие условия достаточны для существования тройного интеграла?",
                    answer = "Функция непрерывна в ограниченной области с кусочно-гладкой границей",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "В чем смысл замены переменных в тройном интеграле?",
                    answer = "Упростить вычисления за счет перехода к более удобной системе координат",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Что означает диаметр разбиения в определении интеграла?",
                    answer = "Наибольшая диагональ среди всех элементов разбиения",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Если F(x) - первообразная для f(x), как выглядит общий вид для всех ее первообразных?",
                    answer = " Если F(x) – одна из первообразных для функции f(x), то выражение F(x) + C, где С – произвольная постоянная, представляет собой общий вид для всех ее первообразных.",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 2,
                    question = "Что вычисляет интеграл?",
                    answer = "Площадь под кривой или первообразную функции",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Что такое криволинейный интеграл функции f(M) по длине дуги L?",
                    answer = "Предел интегральной суммы при условии, что диаметр разбиения стремится к нулю.",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 2,
                    question = "Как еще называют криволинейный интеграл по длине дуги?",
                    answer = "Криволинейный интеграл первого рода",
                    isBuiltIn = true
                ),

                Card(
                    deckId = 2,
                    question = "Какая кривая называется гладкой?",
                    answer = "Кривая, в каждой точке которой существует касательная, и положение касательной непрерывно меняется при перемещении точки вдоль кривой",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое класс?",
                    answer = "Шаблон для создания объектов",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое объект?",
                    answer = "Экземпляр класса",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое функция?",
                    answer = "Блок кода, который выполняет действие",
                    isBuiltIn = true
                ),



                Card(
                    deckId = 3,
                    question = "Опишите жизненный цикл объекта",
                    answer = "Конструктор - использование - деструктор",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое конструктор?",
                    answer = "Это специальный метод класса, создающий объект этого класса",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое деструктор?",
                    answer = "Это специальный метод класса, уничтожающий объект этого класса",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое полиморфизм?",
                    answer = "Это возможность объектов разных классов реагировать на один и тот же вызов метода по-разному",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое икапсуляция?",
                    answer = "Это объединение данных и методов, работающих с этими данными, в одном объекте, скрывая внутреннюю реализацию от внешнего мира",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое конструктор копирования?",
                    answer = "Это специальный конструктор, создающий новый объект как копию существующего объекта того же класса",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое cсылка?",
                    answer = "Это способ неявно указывать на ячейку памяти, где хранится значение переменной, позволяя обращаться к ней по другому имени",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое указатель?",
                    answer = "Это переменные, хранящие адрес ячейки памяти, где находится значение другой переменной, обеспечивая прямой доступ к памяти",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое this?",
                    answer = "Это указатель на текущий объект",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое встраиваемая функция?",
                    answer = "Это функция, код которой при компиляции подставляется непосредственно в место вызова, что позволяет избежать накладных расходов на вызов функции.в",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое дружественная функция?",
                    answer = "Это функция, не являющаяся членом класса, но имеющая доступ к его private и protected членам",
                    isBuiltIn = true
                ),
                Card(
                    deckId = 3,
                    question = "Что такое дружественный класс?",
                    answer = "Это класс, которому разрешен доступ к private и protected членам другого класса",
                    isBuiltIn = true
                )
            )
        }*/
    }
}