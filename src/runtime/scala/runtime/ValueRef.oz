functor

import
   OzmaRuntime('NewObject':NewObject) at '../../scala/ozma/OzmaRuntime.ozf'
   `functor:java.lang.Object`('type:java.lang.Object':`type:java.lang.Object`
                              'class:java.lang.Object':`class:java.lang.Object`) at 'Object.ozf'
   `functor:java.lang.Class`('type:java.lang.Class':`type:java.lang.Class`
                             'class:java.lang.Class':`class:java.lang.Class`) at 'Class.ozf'

export
   MakeValueRefClass

define

   proc {MakeValueRefClass Name InitName ToStringName Module ?Type ?Class}
      class Type from `type:java.lang.Object`
         attr
            elem

         meth !InitName(Value $)
            elem := Value
            `type:java.lang.Object`, '<init>#1063877011'($)
         end

         meth toString($)
            {Module ToStringName(@elem $)}
         end
      end

      Class = {ByNeed fun {$}
                         {NewObject `type:java.lang.Class`
                          `class:java.lang.Class`
                          '<init>'(Name
                                   `class:java.lang.Object`
                                   nil
                                   [`class:java.lang.Object`]
                                   _)}
                      end}
   end

end
