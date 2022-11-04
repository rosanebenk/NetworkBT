package fr.istic.mob.networkBT

import android.app.Dialog
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.*


class GraphView(context: Context?, attrs: AttributeSet?) : View(context, attrs)  {


    var posx = 0f
    var posy = 0f
    var width = 30.0f
    var height = 30.0f

    var mPaint: Paint = Paint()
    var TxtPaint: Paint = Paint()
    var Trait: Paint = Paint()


    var graphe: Graph = Graph()
    var path = Path()
    var temp = Path()

    var objet1: Objet? = null
    var objet2: Objet? = null
    var objetModif: Objet? = null

    var status = "default"

    private var name: String = ""

    var gestureDetector: GestureDetector =
        GestureDetector(context, object : SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent): Boolean {
                return true // nécessaire !!!
            }

            override fun onLongPress(event: MotionEvent) {

                super.onLongPress(event)
                if (status == "ajouter_obj") {

                    posx = event.x
                    posy = event.y
                    popup_objet()
                    name = "" //On remet le nom a vide
                }
                // modifier objet

                // modifier conx
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return super.onFling(e1, e2, distanceX, distanceY)
            }

            override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                if (status=="modifier_obj"){
                    if (e != null) {
                        objetModif = ObjetProche(e.x, e.y)
                    }
                    if (objet1 !== null) {
                        popup_objet()
                    }
                }
                    return super.onDoubleTapEvent(e)
                }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (status == "connecter_obj") {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    objet1 = ObjetProche(event.x, event.y)
                    if (objet1 !== null) {
                        temp.moveTo(objet1!!.px, objet1!!.py)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    objet2 = ObjetProche(event.x, event.y)
                    if (objet2 != null && objet1 != objet2 && objet1 != null) {
                        //var nameCo = popup()
                        //popup()
                        popup_connexion()
                    }
                    temp.reset()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (objet1 != null) {
                        temp.lineTo(event.x, event.y)
                    }
                }
            }
        }

        if (status == "modifier_obj") {
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    objetModif = ObjetProche(event.x, event.y)
                }
                MotionEvent.ACTION_UP -> {
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (objetModif != null) {
                        // if x est dnas le screen faire tt ca
                        if (event.x + width <= this.measuredWidth && event.y - width/2 <= this.measuredHeight
                            && event.x + width >= 0 && event.y - width/2  >= 0) {
                            //Actualiser les coordonnées de l'objet
                            var nomObj = objetModif!!.etiquette.toString()
                            objetModif!!.px = event.x
                            objetModif!!.py = event.y
                            graphe.myObjects.set(nomObj, objetModif!!)
                        }
                    }

                }
            }
        }

        invalidate()
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        //dessin d'objet
        for (dessin in graphe.myObjects.values) {
            if (dessin.etiquette != "") {
                canvas.drawCircle(dessin.px, dessin.py, width, dessin.couleur)
                canvas.drawText(dessin.etiquette, dessin.px, dessin.py, TxtPaint)
                Log.e("COULEUR : ", dessin.etiquette + dessin.couleur.toString())
            }
        }
        //dessin de connexion
        for (connexion in graphe.myConnexions.values) {
            Log.i("nb cnx", graphe.myConnexions.toString())

            path.moveTo(connexion.objet1.px, connexion.objet1.py)
            path.lineTo(connexion.objet2.px, connexion.objet2.py)
            //canvas.drawLine(connexion.objet1.px, connexion.objet1.py, connexion.objet2.px, connexion.objet2.py, Trait)
            canvas.drawPath(path, connexion.color)
            canvas.drawText(
                connexion.name,
                (connexion.objet1.px + connexion.objet2.px) / 2,
                (connexion.objet1.py + connexion.objet2.py) / 2,
                TxtPaint
            )
            path.reset()
        }
        canvas.drawPath(temp, Trait)
    }


    init {
        val TxtSize = 12 * resources.displayMetrics.scaledDensity
        //mPaint.color = graphe.colorofobject
        mPaint.style = Paint.Style.FILL_AND_STROKE
        TxtPaint.color = Color.BLACK
        TxtPaint.style = Paint.Style.FILL_AND_STROKE
        TxtPaint.textSize = TxtSize
        Trait.strokeWidth = graphe.strokeWidth
        //Trait.color = graphe.colorofpath
        Trait.style = Paint.Style.STROKE
    }

    fun reinitialize() {
        graphe.Reinitialize()
        postInvalidate()
    }

    fun ObjetProche(px: Float, py: Float): Objet? {
        for (objet in graphe.myObjects.values) {
            if (objet.px < px + 25 && objet.px > px - 25) {
                if (objet.py < py + 25 && objet.py > py - 25) {
                    Log.e("Trouvé", "objet1")
                    return objet
                }
            }
        }
        return null
    }

    fun popup_objet() {

        val dialog = Dialog(context)
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true)
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.dialog_object)

        //Initializing the views of the dialog.
        val name_obj = dialog.findViewById<EditText>(R.id.obj_name)
        //Radio btn + image a ajouter
        val color_SeekBar = dialog.findViewById<SeekBar>(R.id.seekBar_color)
        val color_text = dialog.findViewById<TextView>(R.id.selected_color)
        //Couleur par défaut
        color_text.text= resources.getString(R.string.green)
        color_text.setTextColor(Color.parseColor("#008000"))
        mPaint.color = Color.GREEN

        color_SeekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                when(progress){
                    0->{
                        color_text.text= resources.getString(R.string.red)
                        color_text.setTextColor(Color.parseColor("#FF0000"))
                        mPaint.color = Color.RED
                    }
                    1->{
                        color_text.text= resources.getString(R.string.green)
                        color_text.setTextColor(Color.parseColor("#008000"))
                        mPaint.color = Color.GREEN
                    }
                    2->{
                        color_text.text= resources.getString(R.string.blue)
                        color_text.setTextColor(Color.parseColor("#0000FF"))
                        mPaint.color = Color.BLUE
                    }
                    3->{
                        color_text.text= resources.getString(R.string.yellow)
                        color_text.setTextColor(Color.parseColor("#FFFF00"))
                        mPaint.color = Color.YELLOW
                    }
                    4->{
                        color_text.text= resources.getString(R.string.cyan)
                        color_text.setTextColor(Color.parseColor("#E0FFFF"))
                        mPaint.color = Color.CYAN
                    }
                    5->{
                        color_text.text= resources.getString(R.string.pink)
                        color_text.setTextColor(Color.parseColor("#FF00FF"))
                        mPaint.color = Color.MAGENTA
                    }
                    6->{
                        color_text.text= resources.getString(R.string.black)
                        color_text.setTextColor(Color.parseColor("#000000"))
                        mPaint.color = Color.BLACK
                    }
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                /*Toast.makeText(context,
                    "Progress is: " + seek.progress.toFloat() + "%",
                    Toast.LENGTH_SHORT).show()*/
                when(seek.progress){
                    0->{
                        mPaint.color = Color.RED
                    }
                    1->{
                        mPaint.color = Color.GREEN
                    }
                    2->{
                        mPaint.color = Color.BLUE
                    }
                    3->{
                        mPaint.color = Color.YELLOW
                    }
                    4->{
                        mPaint.color = Color.CYAN
                    }
                    5->{
                        mPaint.color = Color.MAGENTA
                    }
                    6->{
                        mPaint.color = Color.BLACK
                    }
                }
            }
        })

        val submitButton = dialog.findViewById<Button>(R.id.Btn_validate)
        val cancelButton = dialog.findViewById<Button>(R.id.Btn_cancel)
        submitButton.setOnClickListener {

            //val name = name_obj.text.toString()
            //Radio btn + image a ajouter
            graphe.addObject(name_obj.text.toString(),mPaint,"null", posx, posy)
            invalidate()
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun popup_connexion() {

        val dialog = Dialog(context)
        //We have added a title in the custom layout. So let's disable the default title.
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
        dialog.setCancelable(true)
        //Mention the name of the layout of your custom dialog.
        dialog.setContentView(R.layout.dialog_cnx)

        //Initializing the views of the dialog.
        val name = dialog.findViewById<EditText>(R.id.cnx_name)
        val seekBar = dialog.findViewById<SeekBar>(R.id.seekBar)
        val indicateur_SeekBar = dialog.findViewById<TextView>(R.id.connexion_width)
        seekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                //Affichage de l'épaisseur
                var prog : Float = (progress).toFloat()
                prog /= 2
                indicateur_SeekBar.text = prog.toString()

            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                Trait.strokeWidth = seek.progress.toFloat()/2
            }
        })

        val cnx_color_SeekBar = dialog.findViewById<SeekBar>(R.id.seekBar2)
        val cnx_color_text = dialog.findViewById<TextView>(R.id.cnx_selected_color)
        //Couleur par défaut
        cnx_color_text.text= resources.getString(R.string.green)
        cnx_color_text.setTextColor(Color.parseColor("#008000"))
        Trait.color = Color.GREEN

        cnx_color_SeekBar?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                when(progress){
                    0->{
                        cnx_color_text.text= resources.getString(R.string.red)
                        cnx_color_text.setTextColor(Color.parseColor("#FF0000"))
                        Trait.color = Color.RED
                    }
                    1->{
                        cnx_color_text.text= resources.getString(R.string.green)
                        cnx_color_text.setTextColor(Color.parseColor("#008000"))
                        Trait.color = Color.GREEN
                    }
                    2->{
                        cnx_color_text.text= resources.getString(R.string.blue)
                        cnx_color_text.setTextColor(Color.parseColor("#0000FF"))
                        Trait.color = Color.BLUE
                    }
                    3->{
                        cnx_color_text.text= resources.getString(R.string.yellow)
                        cnx_color_text.setTextColor(Color.parseColor("#FFFF00"))
                        Trait.color = Color.YELLOW
                    }
                    4->{
                        cnx_color_text.text= resources.getString(R.string.cyan)
                        cnx_color_text.setTextColor(Color.parseColor("#E0FFFF"))
                        Trait.color = Color.CYAN
                    }
                    5->{
                        cnx_color_text.text= resources.getString(R.string.pink)
                        cnx_color_text.setTextColor(Color.parseColor("#FF00FF"))
                        Trait.color = Color.MAGENTA
                    }
                    6->{
                        cnx_color_text.text= resources.getString(R.string.black)
                        cnx_color_text.setTextColor(Color.parseColor("#000000"))
                        Trait.color = Color.BLACK
                    }
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
                /*Toast.makeText(context,
                    "Progress is: " + seek.progress.toFloat() + "%",
                    Toast.LENGTH_SHORT).show()*/
                when(seek.progress){
                    0->{
                        Trait.color = Color.RED
                    }
                    1->{
                        Trait.color = Color.GREEN
                    }
                    2->{
                        Trait.color = Color.BLUE
                    }
                    3->{
                        Trait.color = Color.YELLOW
                    }
                    4->{
                        Trait.color = Color.CYAN
                    }
                    5->{
                        Trait.color = Color.MAGENTA
                    }
                    6->{
                        Trait.color = Color.BLACK
                    }
                }
            }
        })

        val submitButton = dialog.findViewById<Button>(R.id.Btn_validate)
        val cancelButton = dialog.findViewById<Button>(R.id.Btn_cancel)



        submitButton.setOnClickListener {
            //val name = name_obj.text.toString()
            //Radio btn + image a ajouter
            graphe.addConnexion(name.text.toString(),Trait,
                seekBar.progress.toFloat(),objet1!!, objet2!!)
            invalidate()
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}

